package com.mentricstudios.cidquest

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ui.theme.AccentOrange
import com.mentricstudios.cidquest.ui.theme.AccentTeal
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary

@Composable
fun ComingSoonScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar()

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedMazeLogo()

                    Text(
                        text = "CID",
                        color = TextPrimary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "QUEST",
                        color = AccentTeal,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp
                    )

                    Text(
                        text = "Find your way through the maze.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            ComingSoonBadge()

            Text(
                text = "New puzzles, every level a new path.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentTeal.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("★", color = AccentTeal, fontSize = 18.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AccentOrange.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("EARLY ACCESS SOON", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ComingSoonBadge() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AccentTeal.copy(alpha = 0.12f))
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "COMING SOON",
            color = AccentTeal,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
    }
}

/**
 * Draws an animated magnifying-glass mark using Canvas — the maze path from
 * the original logo now sits inside the lens, with a glowing dot travelling
 * it on a loop, so the mark reads as "investigating a maze": CID Quest's
 * detective framing over the same underlying game. Fully original geometry,
 * not modeled on any existing show or app's mark.
 */
@Composable
private fun AnimatedMazeLogo() {
    val transition = rememberInfiniteTransition(label = "maze_pulse")
    val glow by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(
        modifier = Modifier
            .size(180.dp)
            .aspectRatio(1f)
            .padding(bottom = 24.dp)
    ) {
        val w = size.width
        val h = size.height
        val lensCenter = Offset(w * 0.42f, h * 0.40f)
        val lensRadius = w * 0.30f

        // Small maze zigzag, sized to sit comfortably inside the lens.
        val mazeStroke = Stroke(width = w * 0.028f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val mazePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(lensCenter.x - lensRadius * 0.55f, lensCenter.y - lensRadius * 0.5f)
            lineTo(lensCenter.x - lensRadius * 0.05f, lensCenter.y - lensRadius * 0.5f)
            lineTo(lensCenter.x - lensRadius * 0.05f, lensCenter.y - lensRadius * 0.05f)
            lineTo(lensCenter.x + lensRadius * 0.5f, lensCenter.y - lensRadius * 0.05f)
            lineTo(lensCenter.x + lensRadius * 0.5f, lensCenter.y + lensRadius * 0.5f)
            lineTo(lensCenter.x - lensRadius * 0.35f, lensCenter.y + lensRadius * 0.5f)
            lineTo(lensCenter.x - lensRadius * 0.35f, lensCenter.y + lensRadius * 0.1f)
        }
        drawPath(path = mazePath, color = AccentTeal, style = mazeStroke)

        drawCircle(
            color = AccentOrange.copy(alpha = glow),
            radius = w * 0.045f * glow,
            center = Offset(lensCenter.x - lensRadius * 0.35f, lensCenter.y + lensRadius * 0.1f)
        )

        // Lens rim.
        drawCircle(
            color = TextPrimary,
            radius = lensRadius,
            center = lensCenter,
            style = Stroke(width = w * 0.045f)
        )

        // Handle, angled down-right off the rim at 45 degrees.
        val handleStart = Offset(
            lensCenter.x + lensRadius * 0.7071f,
            lensCenter.y + lensRadius * 0.7071f
        )
        val handleEnd = Offset(w * 0.88f, h * 0.90f)
        drawLine(
            color = TextPrimary,
            start = handleStart,
            end = handleEnd,
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
    }
}
