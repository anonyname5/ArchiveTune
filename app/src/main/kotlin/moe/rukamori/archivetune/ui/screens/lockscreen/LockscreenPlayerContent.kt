/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.screens.lockscreen

import android.content.Context
import android.media.AudioManager
import android.text.format.DateFormat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.models.MediaMetadata
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun LockscreenPlayerContent(
    mediaMetadata: MediaMetadata,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Clock & Date
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = LocalTime.now()
            currentDate = LocalDate.now()
            delay(1000L)
        }
    }

    val is24Hour = DateFormat.is24HourFormat(context)
    val timeFormatter = remember(is24Hour) {
        DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm")
    }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, MMMM d")
    }

    // 2. Dynamic Dominant Color Extraction from Album Art
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

    // 3. Audio Volume Control
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

    // 4. Position & Duration Calculations
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    val currentPositionMs = if (isSeeking) (seekPosition * duration).toLong() else position
    val safeDuration = duration.coerceAtLeast(1L)
    val progressFraction = (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    val elapsedSeconds = (currentPositionMs / 1000L).coerceAtLeast(0L)
    val remainingSeconds = ((safeDuration - currentPositionMs) / 1000L).coerceAtLeast(0L)
    val elapsedStr = "${elapsedSeconds / 60}:${(elapsedSeconds % 60).toString().padStart(2, '0')}"
    val remainingStr = "-${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"

    // 5. Swipe-to-Dismiss / Unlock Drag Animation
    val dismissOffsetY = remember { Animatable(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    // Pulsing Ambient Backglow Transition
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

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black)
                .offset { IntOffset(0, dismissOffsetY.value.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            totalDragY += dragAmount
                            if (totalDragY < 0) {
                                scope.launch {
                                    dismissOffsetY.snapTo(totalDragY)
                                }
                            }
                        },
                        onDragEnd = {
                            if (totalDragY < -150f) {
                                scope.launch {
                                    dismissOffsetY.animateTo(
                                        targetValue = -1200f,
                                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                                    )
                                    onDismiss()
                                }
                            } else {
                                scope.launch {
                                    dismissOffsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(250, easing = FastOutSlowInEasing),
                                    )
                                    totalDragY = 0f
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                dismissOffsetY.animateTo(0f)
                                totalDragY = 0f
                            }
                        },
                    )
                },
    ) {
        // ─────────────────────────────────────────────────────────────────
        // Layer 1: Fullscreen Blurred Album Artwork Wallpaper
        // ─────────────────────────────────────────────────────────────────
        if (mediaMetadata.thumbnailUrl != null) {
            AsyncImage(
                model = mediaMetadata.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(32.dp)
                        .alpha(0.55f),
            )
        }

        // Layer 2: Dark Vignette Gradient
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x990A0B12),
                                Color(0xBB08090F),
                                Color(0xEE040407),
                            ),
                        ),
                    ),
        )

        // Layer 3: Dynamic Liquid Ambient Backlight Aura
        if (dominantColor != null) {
            val aura = dominantColor!!
            Box(
                modifier =
                    Modifier
                        .size(340.dp)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            alpha = auraAlpha
                        }
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        aura.copy(alpha = 0.65f),
                                        aura.copy(alpha = 0.20f),
                                        Color.Transparent,
                                    ),
                                ),
                            )
                        },
            )
        }

        // ─────────────────────────────────────────────────────────────────
        // Layer 4: Foreground Content
        // ─────────────────────────────────────────────────────────────────
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ─────────────────────────────────────────────────────────────
            // Top: System Lockscreen Header (Padlock, Clock, Date)
            // ─────────────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                // Padlock Icon
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp),
                )

                Spacer(Modifier.height(8.dp))

                // Time Typography (SF Pro style bold thin)
                Text(
                    text = currentTime.format(timeFormatter),
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.shadow(8.dp, CircleShape),
                )

                Spacer(Modifier.height(2.dp))

                // Date
                Text(
                    text = currentDate.format(dateFormatter),
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp,
                )
            }

            // ─────────────────────────────────────────────────────────────
            // Center: Apple Liquid Glass Player Card
            // ─────────────────────────────────────────────────────────────
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0x55FFFFFF)) // Layer 1: Outer specular rim
                        .padding(1.2.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(27.dp))
                            .background(Color(0x30000000)) // Layer 2: 3D refraction shadow
                            .padding(0.8.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color(0x6512131C)), // Layer 3: Smoked crystal obsidian base
                    ) {
                        // Dynamic Liquid Artwork Overlay inside glass
                        if (dominantColor != null) {
                            Box(
                                modifier =
                                    Modifier
                                        .matchParentSize()
                                        .background(dominantColor!!.copy(alpha = 0.18f)),
                            )
                        }

                        // Glass Inner Content
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            // Top Bevel Specular Light Ray
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
                                                    Color(0x90FFFFFF),
                                                    Color.Transparent,
                                                ),
                                            ),
                                        ),
                            )

                            Spacer(Modifier.height(12.dp))

                            // 1. Header: Squircle Artwork + Metadata + AirPlay Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Squircle Artwork
                                Box(
                                    modifier =
                                        Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(15.dp))
                                            .background(Color(0x50FFFFFF))
                                            .padding(1.dp),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0x40000000))
                                                .padding(0.8.dp),
                                    ) {
                                        AsyncImage(
                                            model = mediaMetadata.thumbnailUrl,
                                            contentDescription = "Cover",
                                            contentScale = ContentScale.Crop,
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(13.dp)),
                                        )
                                    }
                                }

                                Spacer(Modifier.width(14.dp))

                                // Track Title & Artist
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = mediaMetadata.title.ifEmpty { stringResource(R.string.app_name) },
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee(),
                                    )

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        text = mediaMetadata.artists.joinToString { it.name }.ifEmpty { "ArchiveTune" },
                                        color = Color.White.copy(alpha = 0.70f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee(),
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                // Apple AirPlay Destination Pill
                                Box(
                                    modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0x50FFFFFF))
                                            .padding(1.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier =
                                            Modifier
                                                .clip(RoundedCornerShape(13.dp))
                                                .background(Color(0x35FFFFFF))
                                                .padding(horizontal = 9.dp, vertical = 4.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_apple_airplay),
                                            contentDescription = "AirPlay",
                                            tint = Color(0xFF38A3FF),
                                            modifier = Modifier.size(13.dp),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "AirPlay",
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // 2. Interactive Precision Scrubber
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

                            // Monospace Tabular Timestamps
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = elapsedStr,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                                Text(
                                    text = remainingStr,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // 3. Apple SF Media Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Backward Chevron
                                Box(
                                    modifier =
                                        Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                enabled = canSkipPrevious,
                                                onClick = onSkipPrevious,
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_backward),
                                        contentDescription = "Previous",
                                        tint = if (canSkipPrevious) Color.White else Color.White.copy(alpha = 0.35f),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }

                                Spacer(Modifier.width(36.dp))

                                // Play / Pause Frosted Glass Lens
                                Box(
                                    modifier =
                                        Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x80FFFFFF))
                                            .padding(1.2.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(dominantColor?.copy(alpha = 0.40f) ?: Color(0x35FFFFFF))
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = onPlayPause,
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
                                            modifier = Modifier.size(26.dp),
                                        )
                                    }
                                }

                                Spacer(Modifier.width(36.dp))

                                // Forward Chevron
                                Box(
                                    modifier =
                                        Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                enabled = canSkipNext,
                                                onClick = onSkipNext,
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_forward),
                                        contentDescription = "Next",
                                        tint = if (canSkipNext) Color.White else Color.White.copy(alpha = 0.35f),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // 4. Apple Lock Screen Volume Deck
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_apple_volume_min),
                                    contentDescription = "Min Volume",
                                    tint = Color.White.copy(alpha = 0.65f),
                                    modifier = Modifier.size(13.dp),
                                )

                                Spacer(Modifier.width(10.dp))

                                // Sleek Volume Slider
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0x28FFFFFF)),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth(currentVolumeProgress)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color.White.copy(alpha = 0.85f)),
                                    )
                                }

                                Spacer(Modifier.width(10.dp))

                                Icon(
                                    painter = painterResource(R.drawable.ic_apple_volume_max),
                                    contentDescription = "Max Volume",
                                    tint = Color.White.copy(alpha = 0.65f),
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // Bottom: Swipe-Up Unlock Zone
            // ─────────────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .padding(bottom = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
            ) {
                Text(
                    text = stringResource(R.string.lockscreen_swipe_up_hint),
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                )

                Spacer(Modifier.height(8.dp))

                // Bottom Home/Unlock Bar Indicator
                Box(
                    modifier =
                        Modifier
                            .width(135.dp)
                            .height(4.5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.65f)),
                )
            }
        }
    }
}
