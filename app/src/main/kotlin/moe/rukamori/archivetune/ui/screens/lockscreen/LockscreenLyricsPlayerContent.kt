/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.screens.lockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.lyrics.LyricsEntry
import moe.rukamori.archivetune.lyrics.LyricsUtils
import moe.rukamori.archivetune.models.MediaMetadata
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
private fun rememberNativeBlurEffect(radius: Float): RenderEffect? {
    val safeRadius = radius.coerceIn(0f, 48f)
    return remember(safeRadius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && safeRadius > 0.5f) {
            BlurEffect(
                radiusX = safeRadius,
                radiusY = safeRadius,
                edgeTreatment = TileMode.Clamp,
            ).takeIf(RenderEffect::isSupported)
        } else {
            null
        }
    }
}

private fun Modifier.nativeBlur(effect: RenderEffect?): Modifier =
    if (effect == null) {
        this
    } else {
        graphicsLayer {
            renderEffect = effect
        }
    }

@Composable
fun LockscreenLyricsPlayerContent(
    mediaMetadata: MediaMetadata,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    lyrics: List<LyricsEntry>,
    hasLyrics: Boolean,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Dominant Artwork Color Extraction
    var dominantColor by remember { mutableStateOf<Color?>(null) }
    LaunchedEffect(mediaMetadata.thumbnailUrl) {
        val url = mediaMetadata.thumbnailUrl ?: return@LaunchedEffect
        val request =
            ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
        val result = context.imageLoader.execute(request)
        val bitmap = result.image?.toBitmap() ?: return@LaunchedEffect
        Palette.from(bitmap).generate { palette ->
            dominantColor = palette?.dominantSwatch?.rgb?.let { Color(it) }
                ?: palette?.vibrantSwatch?.rgb?.let { Color(it) }
        }
    }

    // 2. Audio Volume Management
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember(audioManager) {
        audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
    }
    var currentVolumeProgress by remember {
        mutableFloatStateOf(
            ((audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (maxVolume / 2)).toFloat() / maxVolume.toFloat())
                .coerceIn(0f, 1f),
        )
    }

    DisposableEffect(context, audioManager, maxVolume) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    val cur = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: return
                    currentVolumeProgress = (cur.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
                }
            }
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    // 3. Playback Position & Scrubber Calculations
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    val currentPositionMs = if (isSeeking) (seekPosition * duration).toLong() else position
    val safeDuration = duration.coerceAtLeast(1L)
    val progressFraction = (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    val elapsedSeconds = (currentPositionMs / 1000L).coerceAtLeast(0L)
    val remainingSeconds = ((safeDuration - currentPositionMs) / 1000L).coerceAtLeast(0L)
    val elapsedStr = "${elapsedSeconds / 60}:${(elapsedSeconds % 60).toString().padStart(2, '0')}"
    val remainingStr = "-${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"

    // 4. Swipe-to-Dismiss / Drag Dismissal
    val dismissOffsetY = remember { Animatable(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    // Pulsing Ambient Backglow
    val infiniteTransition = rememberInfiniteTransition(label = "ambientGlow")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "auraAlpha",
    )

    // Active Lyric Index Tracking & Auto-Scroll
    val currentLineIndex = remember(lyrics, currentPositionMs) {
        if (lyrics.isEmpty()) -1 else LyricsUtils.findCurrentLineIndex(lyrics, currentPositionMs, leadMs = 150L)
    }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && !lazyListState.isScrollInProgress) {
            val target = (currentLineIndex - 1).coerceAtLeast(0)
            lazyListState.animateScrollToItem(
                index = target,
                scrollOffset = 0,
            )
        }
    }

    val cardBlur = rememberNativeBlurEffect(36f)

    // Root Container: Translucent Scrim with Clearance for System Clock
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0x35000000))
                .statusBarsPadding()
                .navigationBarsPadding()
                .pointerInput(Unit) {
                    var accumulatedDragY = 0f
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            accumulatedDragY += dragAmount
                        },
                        onDragEnd = {
                            if (accumulatedDragY < -40f) {
                                onDismiss()
                            }
                            accumulatedDragY = 0f
                        },
                        onDragCancel = {
                            accumulatedDragY = 0f
                        },
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        contentAlignment = Alignment.Center,
    ) {
        // Sticky 4x4 Liquid Glass Lyrics Deck (Fixed at Center)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}, // Prevents taps on the card itself from dismissing
                    ),
        ) {
            // Dynamic Liquid Ambient Aura behind card
            if (dominantColor != null) {
                val aura = dominantColor!!
                Box(
                    modifier =
                        Modifier
                            .size(380.dp)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                alpha = auraAlpha
                            }
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            aura.copy(alpha = 0.55f),
                                            aura.copy(alpha = 0.18f),
                                            Color.Transparent,
                                        ),
                                    ),
                                )
                            },
                )
            }

            // Authentic Apple Liquid Crystal Glass Card Frame
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(32.dp, RoundedCornerShape(28.dp), spotColor = dominantColor ?: Color.Black)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x85FFFFFF), // Specular top rim
                                    Color(0x45FFFFFF), // Soft translucent mid rim
                                    Color(0x22FFFFFF), // Base refractive bottom rim
                                ),
                            ),
                        ) // Layer 1: Specular refractive rim
                        .padding(1.4.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.8.dp))
                            .background(Color(0x38000000)) // Layer 2: 3D refraction shadow depth
                            .padding(0.8.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color(0x75121422)), // Layer 3: Smoked crystal obsidian base
                    ) {
                        // Layer 4: Blurred Dynamic Artwork Backdrop
                        if (mediaMetadata.thumbnailUrl != null) {
                            AsyncImage(
                                model = mediaMetadata.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .matchParentSize()
                                        .nativeBlur(cardBlur)
                                        .graphicsLayer { alpha = 0.28f },
                            )
                        }

                        // Layer 5: Dynamic Liquid Artwork Dye Layer
                        if (dominantColor != null) {
                            Box(
                                modifier =
                                    Modifier
                                        .matchParentSize()
                                        .background(dominantColor!!.copy(alpha = 0.22f)),
                            )
                        }

                        // Layer 6: Diagonal Crystal Sheen Gradient
                        Box(
                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0x25FFFFFF),
                                                Color(0x0CFFFFFF),
                                                Color(0x02FFFFFF),
                                                Color(0x00FFFFFF),
                                            ),
                                        ),
                                    ),
                        )

                        // Layer 7: Card Content
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            // Top Bevel Specular Reflection Line
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(1.2.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color(0xA5FFFFFF),
                                                    Color.Transparent,
                                                ),
                                            ),
                                        ),
                            )

                            Spacer(Modifier.height(10.dp))

                            // ─────────────────────────────────────────────────────────────
                            // TOP: SYNCHRONIZED LYRICS STAGE
                            // ─────────────────────────────────────────────────────────────
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Stage Header: Artwork Thumbnail, Synced Lyrics Badge & AirPlay Pill
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Mini Artwork Thumbnail with Glass Rim
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x60FFFFFF))
                                                .padding(0.8.dp),
                                    ) {
                                        AsyncImage(
                                            model = mediaMetadata.thumbnailUrl,
                                            contentDescription = mediaMetadata.title,
                                            contentScale = ContentScale.Crop,
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(7.2.dp)),
                                        )
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    // Synced Lyrics Frosted Capsule
                                    Box(
                                        modifier =
                                            Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x25FFFFFF))
                                                .padding(horizontal = 8.dp, vertical = 3.dp),
                                    ) {
                                        Text(
                                            text = if (hasLyrics) "SYNCED LYRICS" else "LIVE PLAYER",
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }

                                    Spacer(Modifier.weight(1f))

                                    // AirPlay Frosted Capsule
                                    Row(
                                        modifier =
                                            Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x25FFFFFF))
                                                .padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_apple_airplay),
                                            contentDescription = "AirPlay",
                                            tint = Color(0xFF38A3FF),
                                            modifier = Modifier.size(11.dp),
                                        )
                                        Spacer(Modifier.width(3.5.dp))
                                        Text(
                                            text = "AirPlay",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Lyrics Waterfall Stage with Top/Bottom Edge Fades
                                if (hasLyrics && lyrics.isNotEmpty()) {
                                    LazyColumn(
                                        state = lazyListState,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 170.dp, max = 210.dp)
                                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                                .drawWithContent {
                                                    drawContent()
                                                    drawRect(
                                                        brush =
                                                            Brush.verticalGradient(
                                                                0f to Color.Transparent,
                                                                0.10f to Color.Black,
                                                                0.90f to Color.Black,
                                                                1f to Color.Transparent,
                                                            ),
                                                        blendMode = BlendMode.DstIn,
                                                    )
                                                },
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        itemsIndexed(lyrics) { index, item ->
                                            val isActive = index == currentLineIndex
                                            val distance = abs(index - currentLineIndex)

                                            val targetAlpha =
                                                when {
                                                    isActive -> 1f
                                                    distance == 1 -> 0.65f
                                                    distance == 2 -> 0.35f
                                                    else -> 0.18f
                                                }
                                            val animatedAlpha by animateFloatAsState(
                                                targetValue = targetAlpha,
                                                animationSpec = tween(250, easing = FastOutSlowInEasing),
                                                label = "lyricAlpha_$index",
                                            )

                                            val targetFontSize =
                                                when {
                                                    isActive -> 18.sp
                                                    distance == 1 -> 14.sp
                                                    else -> 12.sp
                                                }

                                            Text(
                                                text = item.text,
                                                color = Color.White.copy(alpha = animatedAlpha),
                                                fontSize = targetFontSize,
                                                fontWeight = if (isActive) FontWeight.Bold else if (distance == 1) FontWeight.Medium else FontWeight.Normal,
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { onSeek(item.time) }
                                                        .padding(horizontal = 6.dp, vertical = if (isActive) 5.dp else 3.dp),
                                            )
                                        }
                                    }
                                } else {
                                    // Elegant Placeholder when instrumental or loading
                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 150.dp, max = 190.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(
                                            text = "♪",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = mediaMetadata.title.ifBlank { context.getString(R.string.app_name) },
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = mediaMetadata.artists.joinToString { it.name }.ifBlank { context.getString(R.string.lyrics) },
                                            color = Color.White.copy(alpha = 0.65f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }

                            // Frosted Hairline Separator
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color(0x35FFFFFF),
                                                    Color.Transparent,
                                                ),
                                            ),
                                        ),
                            )

                            Spacer(Modifier.height(10.dp))

                            // ─────────────────────────────────────────────────────────────
                            // BOTTOM: CONTROLS SHELF (Seamless on Liquid Crystal Glass)
                            // ─────────────────────────────────────────────────────────────
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Title & Artist
                                Text(
                                    text = mediaMetadata.title.ifBlank { context.getString(R.string.app_name) },
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee(),
                                )
                                Text(
                                    text = mediaMetadata.artists.joinToString { it.name }.ifBlank { context.getString(R.string.no_track_playing) },
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee(),
                                )

                                Spacer(Modifier.height(6.dp))

                                // Interactive Scrubber
                                Slider(
                                    value = if (isSeeking) seekPosition else progressFraction,
                                    onValueChange = {
                                        isSeeking = true
                                        seekPosition = it
                                    },
                                    onValueChangeFinished = {
                                        isSeeking = false
                                        onSeek((seekPosition * safeDuration).toLong())
                                    },
                                    colors =
                                        SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.22f),
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(18.dp),
                                )

                                // Timestamps
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = elapsedStr,
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Normal,
                                    )
                                    Text(
                                        text = remainingStr,
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Normal,
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                // Playback Control Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    // Skip Previous Glass Button
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x22FFFFFF))
                                                .clickable(enabled = canSkipPrevious, onClick = onSkipPrevious),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_apple_backward),
                                            contentDescription = "Previous",
                                            tint = if (canSkipPrevious) Color.White else Color.White.copy(alpha = 0.35f),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }

                                    Spacer(Modifier.width(28.dp))

                                    // Play / Pause 3D Liquid Lens
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(54.dp)
                                                .shadow(12.dp, CircleShape, spotColor = dominantColor ?: Color.White)
                                                .clip(CircleShape)
                                                .background(Color(0x75FFFFFF))
                                                .padding(1.4.dp)
                                                .clickable(onClick = onPlayPause),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .background(Color(0x35000000))
                                                    .padding(0.8.dp),
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .background(
                                                            dominantColor?.copy(alpha = 0.45f)
                                                                ?: Color(0x40FFFFFF),
                                                        ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    painter =
                                                        painterResource(
                                                            if (isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                                        ),
                                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.width(28.dp))

                                    // Skip Next Glass Button
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x22FFFFFF))
                                                .clickable(enabled = canSkipNext, onClick = onSkipNext),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_apple_forward),
                                            contentDescription = "Next",
                                            tint = if (canSkipNext) Color.White else Color.White.copy(alpha = 0.35f),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // Interactive Volume Slider Row
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
                                                    val cur = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: return@clickable
                                                    currentVolumeProgress = (cur.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_apple_volume_min),
                                            contentDescription = "Volume Min",
                                            tint = Color.White.copy(alpha = 0.65f),
                                            modifier = Modifier.size(13.dp),
                                        )
                                    }

                                    Spacer(Modifier.width(6.dp))

                                    BoxWithConstraints(
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .height(28.dp)
                                                .pointerInput(maxVolume) {
                                                    detectTapGestures { offset ->
                                                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                                        currentVolumeProgress = fraction
                                                        val targetVol = (fraction * maxVolume).roundToInt().coerceIn(0, maxVolume)
                                                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                                                    }
                                                }
                                                .pointerInput(maxVolume) {
                                                    detectHorizontalDragGestures(
                                                        onDragStart = { offset ->
                                                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                                            currentVolumeProgress = fraction
                                                            val targetVol = (fraction * maxVolume).roundToInt().coerceIn(0, maxVolume)
                                                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                                                        },
                                                        onHorizontalDrag = { change, _ ->
                                                            change.consume()
                                                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                                            currentVolumeProgress = fraction
                                                            val targetVol = (fraction * maxVolume).roundToInt().coerceIn(0, maxVolume)
                                                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                                                        },
                                                    )
                                                },
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        val barWidth = maxWidth
                                        // Inactive Track
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(Color(0x25FFFFFF)),
                                        )
                                        // Active Track
                                        Box(
                                            modifier =
                                                Modifier
                                                    .width(barWidth * currentVolumeProgress)
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(Color.White.copy(alpha = 0.85f)),
                                        )
                                    }

                                    Spacer(Modifier.width(6.dp))

                                    Box(
                                        modifier =
                                            Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
                                                    val cur = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: return@clickable
                                                    currentVolumeProgress = (cur.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_apple_volume_max),
                                            contentDescription = "Volume Max",
                                            tint = Color.White.copy(alpha = 0.65f),
                                            modifier = Modifier.size(13.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
