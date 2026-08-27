package com.mentricstudios.cidquest.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mentricstudios.cidquest.R
import com.mentricstudios.cidquest.ads.AdsManager
import com.mentricstudios.cidquest.ads.BannerAd
import com.mentricstudios.cidquest.ui.theme.AccentAmber
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.DailyRewards
import com.mentricstudios.cidquest.util.SettingsPrefs
import com.mentricstudios.cidquest.util.SoundManager
import com.mentricstudios.cidquest.util.bounceClick

@Composable
fun HomeScreen(
    starCount: Int = 3,
    onPlay: () -> Unit,
    onSettings: () -> Unit = {},
    onShop: () -> Unit = {}
) {
    val context = LocalContext.current
    // Local copy so a claimed reward updates the star counter immediately,
    // without needing to re-navigate for the caller to recompute totals.
    var displayedStars by remember(starCount) { mutableStateOf(starCount) }
    var showDailyReward by remember { mutableStateOf(false) }
    var canClaimToday by remember { mutableStateOf(DailyRewards.canClaimToday(context)) }
    var soundEnabled by remember { mutableStateOf(SettingsPrefs.isSoundEnabled(context)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        // Top bar overlays the screen without affecting the centering below
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconChip(icon = Icons.Filled.CardGiftcard, showBadge = canClaimToday) { showDailyReward = true }
            StarCounter(count = displayedStars)
        }

        // Logo + Play button, truly centered on the full screen
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeLogoMark()

            Text(
                text = "CID",
                color = TextPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "QUEST",
                color = AccentGold,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 7.sp
            )

            PlayButton(
                modifier = Modifier.padding(top = 48.dp),
                onClick = onPlay
            )

            if (DailyRewards.currentStreak(context) > 0) {
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " ${DailyRewards.currentStreak(context)}-day streak",
                        color = AccentAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bottom icon row + banner ad, stacked and pinned to the bottom
        // regardless of center content.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomIcon(
                    icon = if (soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    onClick = {
                        val newValue = !soundEnabled
                        soundEnabled = newValue
                        SettingsPrefs.setSoundEnabled(context, newValue)
                        if (newValue) SoundManager.playClick(context)
                    }
                )
                BottomIcon(Icons.Filled.Settings, onClick = onSettings)
                BottomIcon(Icons.Filled.ShoppingCart, onClick = onShop)
            }
            BannerAd()

            Text(
                text = "v1.0.0",
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, top = 4.dp)
            )
        }

        if (showDailyReward) {
            DailyRewardDialog(
                onClaim = { reward ->
                    displayedStars += reward
                    canClaimToday = false
                },
                onDismiss = { showDailyReward = false }
            )
        }
    }
}

/**
 * App logo mark shown above the title on the Home screen — the neon maze
 * artwork, breathing gently with a soft glow behind it so it feels alive
 * rather than a static image sitting on the screen.
 */
@Composable
private fun HomeLogoMark(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "logo_breathe")
    val breathe by infinite.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_glow"
    )

    Box(modifier = modifier.size(112.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .graphicsLayer { scaleX = breathe; scaleY = breathe }
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = glowAlpha))
        )
        Image(
            painter = painterResource(id = R.drawable.img_cid_logo),
            contentDescription = "Cid Quest logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(88.dp)
                .graphicsLayer { scaleX = breathe; scaleY = breathe }
        )
    }
}

@Composable
private fun PlayButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    val infinite = rememberInfiniteTransition(label = "play_glow")
    val glowScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        // Soft pulsing glow ring behind the button
        Box(
            modifier = Modifier
                .size((108 * glowScale).dp)
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = 0.10f))
        )

        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = 0.18f))
                .bounceClick(interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play", tint = AccentGold, modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun IconChip(icon: androidx.compose.ui.graphics.vector.ImageVector, showBadge: Boolean = false, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AccentGold.copy(alpha = 0.14f))
                .bounceClick(interactionSource)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(22.dp))
        }
        // Small unclaimed-reward dot — a nudge to come back and tap it, the
        // same pattern players already recognize from every other app.
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(AccentAmber)
            )
        }
    }
}

@Composable
private fun StarCounter(count: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(AccentAmber.copy(alpha = 0.16f))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(19.dp))
        Text(
            text = "  $count",
            color = AccentAmber,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BottomIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(TextSecondary.copy(alpha = 0.10f))
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
    }
}

/**
 * 7-day streak calendar. Missing a day resets you back to day 1 — that
 * "don't break the chain" pressure, paired with a growing reward, is what
 * makes a daily-reward loop actually bring people back rather than just
 * being a one-time bonus.
 */
@Composable
private fun DailyRewardDialog(onClaim: (Int) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val alreadyClaimed = remember { !DailyRewards.canClaimToday(context) }
    // pendingStreakDay() answers "what would the streak become if claimed
    // right now" — correct before claiming, but wrong once today's reward is
    // already claimed (it would fall back to day 1 since the last-claim day
    // is today, not yesterday). In that case the real current streak
    // (currentStreak()) is what should be shown/highlighted instead.
    val streakDay = remember {
        if (alreadyClaimed) DailyRewards.currentStreak(context) else DailyRewards.pendingStreakDay(context)
    }
    var claimed by remember { mutableStateOf(alreadyClaimed) }
    var todaysReward by remember { mutableStateOf(DailyRewards.rewardForDay(streakDay)) }
    var doubled by remember { mutableStateOf(false) }
    var doubling by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(220)) + scaleIn(
                initialScale = 0.82f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(BackgroundBottom, BackgroundTop)))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DAILY REWARD",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (claimed) "Come back tomorrow for day ${(streakDay % 7) + 1}!" else "Claim today's stars — miss a day and the streak resets.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (day in 1..7) {
                        DayCell(
                            day = day,
                            reward = DailyRewards.rewardForDay(day),
                            state = when {
                                day < streakDay -> DayCellState.PAST
                                day == streakDay && claimed -> DayCellState.CLAIMED
                                day == streakDay -> DayCellState.TODAY
                                else -> DayCellState.FUTURE
                            }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (claimed) CardLocked else AccentGold)
                        .bounceClick(interactionSource, pressedScale = if (claimed) 1f else 0.95f)
                        .clickable(
                            enabled = !claimed,
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val reward = DailyRewards.claim(context)
                            if (reward != null) {
                                claimed = true
                                todaysReward = reward
                                onClaim(reward)
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (claimed) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                            Text(text = " CLAIMED", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        } else {
                            Text(text = "CLAIM +${DailyRewards.rewardForDay(streakDay)} ", color = BackgroundTop, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = BackgroundTop, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                // Once today's reward is claimed, offer to double it by
                // watching a rewarded interstitial — a classic, low-friction
                // ad placement since it's purely optional upside on top of
                // something the player already has.
                if (claimed && !doubled && activity != null) {
                    Spacer(Modifier.height(10.dp))
                    val doubleInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AccentAmber.copy(alpha = if (doubling) 0.5f else 1f))
                            .bounceClick(doubleInteraction, pressedScale = 0.95f)
                            .clickable(
                                enabled = !doubling,
                                interactionSource = doubleInteraction,
                                indication = null
                            ) {
                                doubling = true
                                AdsManager.showRewardedInterstitial(
                                    activity = activity,
                                    onReward = {
                                        DailyRewards.grantBonusStars(context, todaysReward)
                                        doubled = true
                                        onClaim(todaysReward)
                                    },
                                    onClosed = { doubling = false },
                                    onNotReady = { doubling = false }
                                )
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (doubling) "LOADING AD…" else "WATCH AD TO DOUBLE (+$todaysReward)",
                            color = BackgroundTop,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

private enum class DayCellState { PAST, TODAY, CLAIMED, FUTURE }

@Composable
private fun DayCell(day: Int, reward: Int, state: DayCellState) {
    val scale = remember { Animatable(if (state == DayCellState.TODAY) 0.85f else 1f) }
    LaunchedEffect(state) {
        if (state == DayCellState.TODAY) {
            scale.animateTo(1.1f, spring(dampingRatio = 0.5f, stiffness = 300f))
            scale.animateTo(1f, tween(120))
        }
    }
    val bg = when (state) {
        DayCellState.PAST, DayCellState.CLAIMED -> AccentGold.copy(alpha = 0.22f)
        DayCellState.TODAY -> AccentAmber.copy(alpha = 0.28f)
        DayCellState.FUTURE -> CardLocked
    }
    val textColor = when (state) {
        DayCellState.TODAY -> AccentAmber
        DayCellState.PAST, DayCellState.CLAIMED -> AccentGold
        DayCellState.FUTURE -> TextSecondary
    }
    Column(
        modifier = Modifier
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "D$day", color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        if (state == DayCellState.PAST || state == DayCellState.CLAIMED) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
        } else {
            Text(text = "$reward", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}
