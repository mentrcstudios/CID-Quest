package com.mentricstudios.cidquest.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ads.BannerAd
import com.mentricstudios.cidquest.ui.theme.AccentOrange
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.CategoryClassic
import com.mentricstudios.cidquest.ui.theme.CategoryDarkness
import com.mentricstudios.cidquest.ui.theme.CategoryIce
import com.mentricstudios.cidquest.ui.theme.CategoryLightning
import com.mentricstudios.cidquest.ui.theme.CategoryTraps
import com.mentricstudios.cidquest.ui.theme.LockGrey
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.bounceClick
import kotlinx.coroutines.delay

data class MazeCategory(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val unlocked: Boolean,
    val progressLabel: String
)

val CATEGORIES = listOf(
    MazeCategory("Classic", Icons.Filled.Extension, CategoryClassic, unlocked = true, progressLabel = ""),
    MazeCategory("Ice Floor", Icons.Filled.AcUnit, CategoryIce, unlocked = true, progressLabel = "0/70"),
    MazeCategory("Darkness", Icons.Filled.DarkMode, CategoryDarkness, unlocked = true, progressLabel = "0/50"),
    MazeCategory("Traps", Icons.Filled.Dangerous, CategoryTraps, unlocked = false, progressLabel = "Locked"),
    MazeCategory("Lightning", Icons.Filled.Bolt, CategoryLightning, unlocked = false, progressLabel = "Locked")
)

@Composable
fun CategoriesScreen(
    starCount: Int = 3,
    classicCompleted: Int = 0,
    classicTotal: Int = 1,
    iceCompleted: Int = 0,
    iceTotal: Int = 70,
    darknessCompleted: Int = 0,
    darknessTotal: Int = 50,
    onCategoryClick: (MazeCategory) -> Unit,
    onBack: () -> Unit
) {
    val categories = remember(classicCompleted, classicTotal, iceCompleted, iceTotal, darknessCompleted, darknessTotal) {
        CATEGORIES.map { category ->
            when (category.name) {
                "Classic" -> category.copy(progressLabel = "$classicCompleted/$classicTotal")
                "Ice Floor" -> category.copy(progressLabel = "$iceCompleted/$iceTotal")
                "Darkness" -> category.copy(progressLabel = "$darknessCompleted/$darknessTotal")
                else -> category
            }
        }
    }

    // Star badge pops in with a little overshoot instead of appearing flat.
    val starPop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        starPop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 280f))
    }

    // One shared shimmer value for every unlocked card instead of each
    // CategoryCard running its own rememberInfiniteTransition. With up to
    // 4 unlocked cards on screen, that was 4 independent infinite animation
    // loops ticking forever in the background — the same class of jank
    // already fixed on the level-select grid and shop screen. A single
    // shared value costs the same as one, no matter how many cards read it.
    //
    // Kept as a State<Float> (no "by") and only unwrapped with .value inside
    // a drawWithCache{} block down in CategoryCard, so the shimmer sweeping
    // doesn't force this whole screen to recompose on every animation frame.
    val shimmer = rememberInfiniteTransition(label = "category_shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val backInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .bounceClick(backInteraction, pressedScale = 0.85f)
                        .clickable(interactionSource = backInteraction, indication = null) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "CATEGORIES",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier
                        .graphicsLayer(scaleX = starPop.value, scaleY = starPop.value)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(13.dp))
                    Text(text = " $starCount", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Column(
                modifier = Modifier.padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    CategoryCard(category = category, index = index, shimmer = shimmer, onClick = { onCategoryClick(category) })
                }

                UnlockBanner()
            }
        }

        BannerAd(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

@Composable
private fun CategoryCard(category: MazeCategory, index: Int, shimmer: androidx.compose.runtime.State<Float>, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    // Cards slide/fade in one after another instead of all popping at once —
    // a small staggered entrance reads as more polished than a hard cut.
    val entrance = remember(category.name) { Animatable(0f) }
    LaunchedEffect(category.name) {
        delay(60L * index)
        entrance.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 260f))
    }

    val cardBrush = if (category.unlocked) {
        Brush.horizontalGradient(listOf(category.color, category.color.copy(alpha = 0.78f)))
    } else {
        Brush.horizontalGradient(listOf(CardLocked, CardLocked))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer(
                alpha = entrance.value,
                translationX = (1f - entrance.value) * 40f,
                scaleX = 0.94f + 0.06f * entrance.value,
                scaleY = 0.94f + 0.06f * entrance.value
            )
            .clip(RoundedCornerShape(16.dp))
            .background(cardBrush)
            .bounceClick(interactionSource)
            .clickable(
                enabled = category.unlocked,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (category.unlocked) 0.20f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (category.unlocked) {
                    // Diagonal shimmer highlight sweeping through the icon badge.
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .drawWithCache {
                                onDrawBehind {
                                    val s = shimmer.value
                                    drawRect(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.22f), Color.Transparent),
                                            start = Offset(s * 90f - 45f, 0f),
                                            end = Offset(s * 90f, 70f)
                                        )
                                    )
                                }
                            }
                    )
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = LockGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = "  ${category.name.uppercase()}",
                color = if (category.unlocked) Color.White else TextSecondary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = if (category.unlocked) 0.16f else 0f))
                .padding(horizontal = if (category.unlocked) 12.dp else 0.dp, vertical = if (category.unlocked) 5.dp else 0.dp)
        ) {
            Text(
                text = category.progressLabel,
                color = if (category.unlocked) Color.White else TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun UnlockBanner() {
    // Soft pulsing glow so this reads as a live "coming soon" teaser rather
    // than a dead, static footnote at the bottom of the list.
    val infinite = rememberInfiniteTransition(label = "banner_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "banner_pulse_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AccentOrange.copy(alpha = pulse))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentOrange.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Filled.RocketLaunch, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(19.dp))
        }
        Text(
            text = "FULL GAME LAUNCHING SOON",
            color = AccentOrange,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
