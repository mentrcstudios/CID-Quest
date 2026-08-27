package com.mentricstudios.cidquest.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ui.theme.AccentAmber
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private val LOADING_MESSAGES = listOf(
    "Carving out corridors…",
    "Hiding a few dead ends…",
    "Polishing the walls…",
    "Placing the exit…",
    "Almost ready…"
)

@Composable
fun LoadingScreen(onFinished: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400, easing = LinearEasing),
        label = "loading_progress"
    )

    var messageIndex by remember { mutableIntStateOf(0) }

    // A brief settle-in for the whole logo block instead of it just being
    // present on the first frame — makes the screen feel considered rather
    // than instant/flat, without adding any real time to the load.
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(420),
        label = "loading_content_alpha"
    )
    val contentOffset by animateFloatAsState(
        targetValue = if (contentVisible) 0f else 14f,
        animationSpec = tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "loading_content_offset"
    )

    LaunchedEffect(Unit) {
        // Simulated loading — replace with real asset/init loading later
        for (step in 1..LOADING_MESSAGES.size) {
            messageIndex = step - 1
            delay(420)
            progress = step / LOADING_MESSAGES.size.toFloat()
        }
        delay(250)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))),
        contentAlignment = Alignment.Center
    ) {
        AmbientGlow()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = contentAlpha
                translationY = contentOffset
            }
        ) {

            AnimatedMazeIcon()

            Text(
                text = "CID QUEST",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                text = "QUEST",
                color = AccentGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp
            )

            Box(
                modifier = Modifier
                    .padding(top = 36.dp)
                    .fillMaxWidth(0.6f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TextSecondary.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(AccentGold, AccentAmber)))
                )
            }

            AnimatedContent(
                targetState = messageIndex,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "loading_message"
            ) { index ->
                Text(
                    text = LOADING_MESSAGES[index],
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

/** Slow, barely-there breathing glow behind the whole logo block — gives the
 * screen a bit of atmosphere instead of a flat dark background while
 * staying subtle enough not to compete with the maze icon animation. */
@Composable
private fun AmbientGlow() {
    val infinite = rememberInfiniteTransition(label = "ambient_glow")
    val glow by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    Box(
        modifier = Modifier
            .size(260.dp)
            .drawWithCache {
                onDrawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(AccentGold.copy(alpha = 0.10f * glow), androidx.compose.ui.graphics.Color.Transparent)
                        )
                    )
                }
            }
    )
}

/**
 * Animated maze icon: a glowing dot travels continuously along the maze
 * path, giving the loading screen personality instead of a plain spinner.
 */
@Composable
private fun AnimatedMazeIcon() {
    val infinite = rememberInfiniteTransition(label = "maze_loading")

    val dotProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dot_progress"
    )

    val pulse by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = Modifier
            .size(110.dp)
            .aspectRatio(1f)
    ) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = w * 0.06f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val path = Path().apply {
            moveTo(w * 0.18f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.82f)
            lineTo(w * 0.18f, h * 0.82f)
            lineTo(w * 0.18f, h * 0.55f)
            lineTo(w * 0.45f, h * 0.55f)
            lineTo(w * 0.45f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.48f)
            lineTo(w * 0.55f, h * 0.48f)
            lineTo(w * 0.55f, h * 0.68f)
            lineTo(w * 0.72f, h * 0.68f)
            lineTo(w * 0.72f, h * 0.28f)
            lineTo(w * 0.28f, h * 0.28f)
            lineTo(w * 0.28f, h * 0.72f)
            lineTo(w * 0.42f, h * 0.72f)
        }

        drawPath(path = path, color = AccentGold.copy(alpha = 0.85f), style = stroke)

        // Compute the point along the path for the given progress fraction
        val measure = PathMeasure()
        measure.setPath(path, false)
        val length = measure.length
        val position = measure.getPosition(length * dotProgress)

        val dotCenter = if (position != Offset.Unspecified) position else Offset(w * 0.18f, h * 0.18f)

        drawCircle(
            color = AccentAmber,
            radius = w * 0.06f * pulse,
            center = dotCenter
        )
        drawCircle(
            color = AccentAmber.copy(alpha = 0.25f),
            radius = w * 0.11f * pulse,
            center = dotCenter
        )
    }
}
