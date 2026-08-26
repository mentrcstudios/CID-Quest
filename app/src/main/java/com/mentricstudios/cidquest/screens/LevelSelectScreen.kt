package com.mentricstudios.cidquest.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ads.BannerAd
import com.mentricstudios.cidquest.game.MazeLevels
import com.mentricstudios.cidquest.ui.theme.AccentOrange
import com.mentricstudios.cidquest.ui.theme.AccentTeal
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.LockGrey
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.GameProgress
import com.mentricstudios.cidquest.util.bounceClick
import kotlinx.coroutines.delay

private data class LevelTileState(
    val levelNumber: Int,
    val playable: Boolean,
    val unlocked: Boolean,
    val earnedStars: Int
)

@Composable
fun LevelSelectScreen(
    categoryName: String,
    starCount: Int = 3,
    totalLevels: Int = 30,
    onLevelClick: (Int) -> Unit = {},
    onBack: () -> Unit
) {
    var showComingSoonDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Locked tiles share ONE breathing pulse instead of each running its own
    // rememberInfiniteTransition. With up to 30 tiles on screen (many of them
    // locked), 30 independent infinite animation loops were running forever
    // in the background, which is what made this screen feel laggy/janky —
    // every frame had to recompute/redraw dozens of animations at once. A
    // single shared value costs the same as one, no matter how many locked
    // tiles read it.
    //
    // IMPORTANT: this is kept as a State<Float> (no "by" here) and only
    // unwrapped with .value inside a graphicsLayer{} block down in LevelTile.
    // Unwrapping it here with "by" would make THIS function re-run on every
    // single animation frame (60x/second) for as long as the screen is open,
    // which in turn re-ran the SharedPreferences/level-lookup work below for
    // every tile on every frame — that per-frame full recompute was the
    // actual cause of the lag on this screen, not the animation itself.
    // Reading .value only inside graphicsLayer{} defers it to the draw
    // phase, so only the pixels are redrawn each frame, not the composition.
    val lockPulse = rememberInfiniteTransition(label = "lock_pulse").animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Reverse),
        label = "lock_pulse_value"
    )

    // Each tile's unlocked/star state depends only on saved progress, so it's
    // computed once per screen visit instead of being recalculated (with a
    // SharedPreferences read plus a level-list lookup) for every tile on
    // every recomposition.
    val tileStates = remember(context, categoryName, totalLevels) {
        (1..totalLevels).map { levelNumber ->
            val playable = MazeLevels.isPlayable(categoryName, levelNumber)
            val unlocked = playable &&
                (levelNumber == 1 || GameProgress.bestStars(context, categoryName, levelNumber - 1) > 0)
            val earnedStars = if (playable) GameProgress.bestStars(context, categoryName, levelNumber) else 0
            LevelTileState(levelNumber, playable, unlocked, earnedStars)
        }
    }

    // Which level numbers have already played their pop-in entrance once.
    // Lives here, at the grid's level, instead of inside each LevelTile —
    // because LazyVerticalGrid disposes/recomposes tiles as they scroll off
    // and back on screen, a tile-local "have I appeared yet" flag was reset
    // every single time, so every tile replayed its full delay+spring
    // entrance animation on every scroll pass. With up to 30 tiles that's
    // what was actually causing the scroll stutter on this screen — this
    // set makes the pop-in a true one-time event per level, not per scroll.
    val seenLevelTiles = remember { HashSet<Int>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 32.dp)
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
                    text = categoryName.uppercase(),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(13.dp))
                    Text(text = " $starCount", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(totalLevels) { index ->
                    val state = tileStates[index]
                    LevelTile(
                        number = state.levelNumber,
                        unlocked = state.unlocked,
                        earnedStars = state.earnedStars,
                        lockPulse = lockPulse,
                        alreadyAppeared = state.levelNumber in seenLevelTiles,
                        onAppeared = { seenLevelTiles.add(state.levelNumber) },
                        onClick = {
                            if (state.unlocked && state.playable) onLevelClick(state.levelNumber) else showComingSoonDialog = true
                        }
                    )
                }
            }
        }

        BannerAd(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }

    if (showComingSoonDialog) {
        ComingSoonOverlay(onDismiss = { showComingSoonDialog = false })
    }
}

@Composable
private fun ComingSoonOverlay(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(androidx.compose.animation.core.tween(200)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 320f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BackgroundBottom)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("COMING SOON", color = AccentOrange, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Maze gameplay for this level is still in development — check back in the full release!",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LevelTile(
    number: Int,
    unlocked: Boolean,
    earnedStars: Int = 0,
    lockPulse: androidx.compose.runtime.State<Float>,
    alreadyAppeared: Boolean = false,
    onAppeared: () -> Unit = {},
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Tiles used to just appear all at once in a flat grid. Now each one
    // pops in with a springy scale/fade, staggered by level number, so the
    // grid reads as sweeping into place left-to-right, top-to-bottom instead
    // of popping into existence as one static block — but only the first
    // time. [alreadyAppeared] seeds the starting state so a tile that's
    // scrolling back into view (after the grid disposed it while it was
    // off-screen) renders instantly settled instead of re-running its
    // delay + spring animation from scratch on every scroll pass.
    var appeared by remember(number) { mutableStateOf(alreadyAppeared) }
    LaunchedEffect(number) {
        if (!appeared) {
            delay((number.coerceAtMost(20) * 16).toLong())
            appeared = true
            onAppeared()
        }
    }
    val entranceScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.4f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 340f),
        label = "tile_scale"
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(220),
        label = "tile_alpha"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = entranceScale
                scaleY = entranceScale
                alpha = entranceAlpha
            }
            .clip(RoundedCornerShape(12.dp))
            .background(if (unlocked) AccentTeal.copy(alpha = 0.18f) else CardLocked)
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (unlocked) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$number", color = AccentTeal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (earnedStars > 0) {
                    Row {
                        repeat(earnedStars) {
                            Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(9.dp))
                        }
                    }
                }
            }
        } else {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked",
                tint = LockGrey,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { alpha = lockPulse.value }
            )
        }
    }
}
