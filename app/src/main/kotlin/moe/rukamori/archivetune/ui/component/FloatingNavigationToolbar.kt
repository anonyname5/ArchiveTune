/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.archivetune.ui.component

import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.LocalPlayerConnection
import moe.rukamori.archivetune.constants.FloatingBarJunctionCornerRadius
import moe.rukamori.archivetune.constants.FloatingBarOuterCornerRadius
import moe.rukamori.archivetune.constants.FloatingBarStandaloneCornerRadius
import moe.rukamori.archivetune.constants.NavigationBarBackgroundStyle
import moe.rukamori.archivetune.constants.NavigationBarBackgroundStyleKey
import moe.rukamori.archivetune.constants.NavigationBarHeight
import moe.rukamori.archivetune.constants.NavigationBarMaxWidth
import moe.rukamori.archivetune.ui.screens.Screens
import moe.rukamori.archivetune.ui.theme.PlayerColorExtractor
import moe.rukamori.archivetune.utils.rememberEnumPreference

private val NavigationItemsMaxWidth = 360.dp
private val NavigationItemVerticalPadding = 8.dp
private const val NavigationBarPaletteCacheSize = 24

@Composable
fun FloatingNavigationToolbar(
    items: List<Screens>,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    miniPlayerProximityProvider: () -> Float = { 0f },
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
    onSearchItemDoubleClick: (() -> Unit)? = null,
) {
    val playerConnection = LocalPlayerConnection.current
    val context = LocalContext.current
    val navigationBarBackgroundStyle by rememberEnumPreference(
        key = NavigationBarBackgroundStyleKey,
        defaultValue = NavigationBarBackgroundStyle.THEME,
    )
    val mediaMetadata by playerConnection?.mediaMetadata?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(null) }
    var gradientColors by remember {
        mutableStateOf<List<Color>>(emptyList())
    }
    val gradientColorsCache =
        remember {
            object : LinkedHashMap<String, List<Color>>(NavigationBarPaletteCacheSize, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Color>>?): Boolean =
                    size > NavigationBarPaletteCacheSize
            }
        }
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()
    val shouldUseArtworkBackground = navigationBarBackgroundStyle != NavigationBarBackgroundStyle.THEME

    LaunchedEffect(
        mediaMetadata?.id,
        mediaMetadata?.thumbnailUrl,
        shouldUseArtworkBackground,
        fallbackColor,
    ) {
        if (!shouldUseArtworkBackground) {
            gradientColors = emptyList()
            return@LaunchedEffect
        }

        val currentMetadata = mediaMetadata
        val thumbnailUrl = currentMetadata?.thumbnailUrl
        if (currentMetadata == null || thumbnailUrl.isNullOrBlank()) {
            gradientColors = emptyList()
            return@LaunchedEffect
        }

        val cachedColors = gradientColorsCache[currentMetadata.id]
        if (cachedColors != null) {
            gradientColors = cachedColors
            return@LaunchedEffect
        }

        val request =
            ImageRequest
                .Builder(context)
                .data(thumbnailUrl)
                .size(PlayerColorExtractor.Config.IMAGE_SIZE, PlayerColorExtractor.Config.IMAGE_SIZE)
                .allowHardware(false)
                .build()

        val extractedColors =
            runCatching {
                val result =
                    withContext(Dispatchers.IO) {
                        context.imageLoader.execute(request)
                    }
                val bitmap = result.image?.toBitmap() ?: return@runCatching emptyList()
                val palette =
                    withContext(Dispatchers.Default) {
                        Palette
                            .from(bitmap)
                            .maximumColorCount(PlayerColorExtractor.Config.MAX_COLOR_COUNT)
                            .resizeBitmapArea(PlayerColorExtractor.Config.BITMAP_AREA)
                            .generate()
                    }
                PlayerColorExtractor.extractGradientColors(
                    palette = palette,
                    fallbackColor = fallbackColor,
                )
            }.getOrDefault(emptyList())

        if (extractedColors.isNotEmpty()) {
            gradientColorsCache[currentMetadata.id] = extractedColors
        }
        gradientColors = extractedColors
    }

    val backgroundPalette =
        remember(gradientColors) {
            NavigationBarBackgroundPalette.from(gradientColors)
        }
    val effectiveBackgroundStyle =
        if (shouldUseArtworkBackground && backgroundPalette != null) {
            navigationBarBackgroundStyle
        } else {
            NavigationBarBackgroundStyle.THEME
        }
    val isArtworkBackground = effectiveBackgroundStyle != NavigationBarBackgroundStyle.THEME

    val miniPlayerProximity = miniPlayerProximityProvider()
    val navigationShape =
        RoundedCornerShape(
            topStart = lerp(FloatingBarStandaloneCornerRadius.value, FloatingBarJunctionCornerRadius.value, miniPlayerProximity).dp,
            topEnd = lerp(FloatingBarStandaloneCornerRadius.value, FloatingBarJunctionCornerRadius.value, miniPlayerProximity).dp,
            bottomStart = lerp(FloatingBarStandaloneCornerRadius.value, FloatingBarOuterCornerRadius.value, miniPlayerProximity).dp,
            bottomEnd = lerp(FloatingBarStandaloneCornerRadius.value, FloatingBarOuterCornerRadius.value, miniPlayerProximity).dp,
        )
    val navigationContainerColor =
        if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    val motionScheme = MaterialTheme.motionScheme

    val itemColors =
        when {
            isArtworkBackground ->
                ShortNavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    selectedIndicatorColor = Color.White.copy(alpha = 0.22f),
                    unselectedIconColor = Color.White.copy(alpha = 0.72f),
                    unselectedTextColor = Color.White.copy(alpha = 0.72f),
                )
            pureBlack ->
                ShortNavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    selectedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                    unselectedTextColor = Color.White.copy(alpha = 0.7f),
                )
            else -> ShortNavigationBarItemDefaults.colors()
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier =
                Modifier
                    .widthIn(max = NavigationBarMaxWidth)
                    .fillMaxWidth()
                    .height(NavigationBarHeight),
            shape = navigationShape,
            color = if (isArtworkBackground) Color.Transparent else navigationContainerColor,
            tonalElevation = NavigationBarDefaults.Elevation,
            shadowElevation = NavigationBarDefaults.Elevation,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isArtworkBackground) {
                    NavigationBarBackground(
                        style = effectiveBackgroundStyle,
                        palette = backgroundPalette,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                ShortNavigationBar(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentColor = if (pureBlack || isArtworkBackground) Color.White else MaterialTheme.colorScheme.onSurface,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    arrangement = ShortNavigationBarArrangement.EqualWeight,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .widthIn(max = NavigationItemsMaxWidth)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(vertical = NavigationItemVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            items.forEach { screen ->
                                val selected = isSelected(screen)
                                val onDoubleClick =
                                    remember(screen, onSearchItemDoubleClick) {
                                        if (screen == Screens.Search) onSearchItemDoubleClick else null
                                    }
                                val lastClickTime = remember(screen) { mutableLongStateOf(0L) }
                                val onClick =
                                    remember(screen, selected, onItemClick, onDoubleClick) {
                                        {
                                            val currentTime = SystemClock.uptimeMillis()
                                            val isDoubleClick =
                                                onDoubleClick != null &&
                                                    currentTime - lastClickTime.longValue <= ViewConfiguration.getDoubleTapTimeout()
                                            lastClickTime.longValue = if (isDoubleClick) 0L else currentTime
                                            if (isDoubleClick) {
                                                onDoubleClick?.invoke()
                                                Unit
                                            } else {
                                                onItemClick(screen, selected)
                                            }
                                        }
                                    }

                                ShortNavigationBarItem(
                                    selected = selected,
                                    onClick = onClick,
                                    modifier = Modifier.weight(1f),
                                    colors = itemColors,
                                    icon = {
                                        Crossfade(
                                            targetState = selected,
                                            animationSpec = motionScheme.fastEffectsSpec(),
                                            label = "navigationItemIcon",
                                        ) { isSelected ->
                                            Icon(
                                                painter =
                                                    painterResource(
                                                        if (isSelected) screen.iconIdActive else screen.iconIdInactive,
                                                    ),
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(screen.titleId),
                                            maxLines = 1,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationBarBackground(
    style: NavigationBarBackgroundStyle,
    palette: NavigationBarBackgroundPalette?,
    modifier: Modifier = Modifier,
) {
    when (style) {
        NavigationBarBackgroundStyle.THEME -> {
            Box(
                modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            )
        }

        NavigationBarBackgroundStyle.GRADIENT -> {
            val colors = requireNotNull(palette)
            Box(modifier = modifier) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops =
                                        arrayOf(
                                            0f to colors.first.copy(alpha = 0.95f),
                                            0.52f to colors.second.copy(alpha = 0.82f),
                                            1f to colors.third.copy(alpha = 0.72f),
                                        ),
                                ),
                            ),
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.32f)),
                )
            }
        }

        NavigationBarBackgroundStyle.GLOW -> {
            val colors = requireNotNull(palette)
            Box(
                modifier =
                    modifier.drawWithCache {
                        val width = size.width
                        val height = size.height
                        val startGlow =
                            Brush.radialGradient(
                                colors = listOf(colors.first.copy(alpha = 0.82f), colors.first.copy(alpha = 0.38f), Color.Transparent),
                                center = Offset(width * 0.12f, height * 0.42f),
                                radius = width * 0.72f,
                            )
                        val endGlow =
                            Brush.radialGradient(
                                colors = listOf(colors.second.copy(alpha = 0.78f), colors.second.copy(alpha = 0.34f), Color.Transparent),
                                center = Offset(width * 0.88f, height * 0.58f),
                                radius = width * 0.72f,
                            )
                        val topGlow =
                            Brush.radialGradient(
                                colors = listOf(colors.third.copy(alpha = 0.58f), Color.Transparent),
                                center = Offset(width * 0.52f, height * 0.05f),
                                radius = width * 0.54f,
                            )
                        val bottomGlow =
                            Brush.radialGradient(
                                colors = listOf(colors.fourth.copy(alpha = 0.46f), Color.Transparent),
                                center = Offset(width * 0.46f, height * 1.05f),
                                radius = width * 0.54f,
                            )

                        onDrawBehind {
                            drawRect(Color.Black)
                            drawRect(startGlow)
                            drawRect(endGlow)
                            drawRect(topGlow)
                            drawRect(bottomGlow)
                            drawRect(Color.Black.copy(alpha = 0.24f))
                        }
                    },
            )
        }
    }
}

@Immutable
private data class NavigationBarBackgroundPalette(
    val first: Color,
    val second: Color,
    val third: Color,
    val fourth: Color,
) {
    companion object {
        fun from(colors: List<Color>): NavigationBarBackgroundPalette? {
            val first = colors.firstOrNull() ?: return null
            val second = colors.getOrElse(1) { first }
            val third = colors.getOrElse(2) { second }
            val fourth = colors.getOrElse(3) { first }
            return NavigationBarBackgroundPalette(
                first = first,
                second = second,
                third = third,
                fourth = fourth,
            )
        }
    }
}
