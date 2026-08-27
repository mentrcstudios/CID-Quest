package com.mentricstudios.cidquest.screens

import android.app.Activity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ads.AdsManager
import com.mentricstudios.cidquest.ads.BannerAd
import com.mentricstudios.cidquest.game.Skin
import com.mentricstudios.cidquest.game.SkinType
import com.mentricstudios.cidquest.game.SkinsCatalog
import com.mentricstudios.cidquest.ui.theme.AccentAmber
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.DailyRewards
import com.mentricstudios.cidquest.util.SkinPrefs
import com.mentricstudios.cidquest.util.bounceClick
import kotlinx.coroutines.delay

/**
 * The Shop — every skin unlocks itself automatically the moment the player's
 * total stars reach its cost. There's no separate "buy" action and stars are
 * never spent; tapping an unlocked skin just equips it for the maze/enemies.
 */
@Composable
fun ShopScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedTab by remember { mutableStateOf(SkinType.PLAYER) }
    var totalStars by remember { mutableStateOf(SkinPrefs.totalStars(context)) }
    var watchingAd by remember { mutableStateOf(false) }
    var equippedPlayerId by remember { mutableStateOf(SkinPrefs.selectedSkinId(context, SkinType.PLAYER)) }
    var equippedEnemyId by remember { mutableStateOf(SkinPrefs.selectedSkinId(context, SkinType.ENEMY)) }

    fun equippedId(type: SkinType) = if (type == SkinType.PLAYER) equippedPlayerId else equippedEnemyId

    fun handleSkinTap(type: SkinType, skin: Skin) {
        // Nothing to purchase — a skin either has enough stars behind it
        // already (tap equips it) or it doesn't (tap does nothing yet).
        if (!SkinPrefs.isUnlocked(context, type, skin.id)) return
        SkinPrefs.select(context, type, skin.id)
        if (type == SkinType.PLAYER) equippedPlayerId = skin.id else equippedEnemyId = skin.id
    }

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
                    text = "SHOP",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 28.dp),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentAmber.copy(alpha = 0.14f))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                Text(
                    text = "  $totalStars STARS EARNED",
                    color = AccentAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "Skins unlock on their own as you earn stars — nothing to buy.",
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )

            if (activity != null) {
                val adInteraction = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentGold.copy(alpha = if (watchingAd) 0.10f else 0.18f))
                        .bounceClick(adInteraction, pressedScale = 0.97f)
                        .clickable(
                            enabled = !watchingAd,
                            interactionSource = adInteraction,
                            indication = null
                        ) {
                            watchingAd = true
                            AdsManager.showRewarded(
                                activity = activity,
                                onReward = {
                                    DailyRewards.grantBonusStars(context, 15)
                                    totalStars = SkinPrefs.totalStars(context)
                                },
                                onClosed = { watchingAd = false },
                                onNotReady = { watchingAd = false }
                            )
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (watchingAd) "LOADING AD…" else "WATCH AD FOR +15 STARS",
                        color = AccentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShopTabButton(
                    label = "PLAYER SKINS",
                    selected = selectedTab == SkinType.PLAYER,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = SkinType.PLAYER }
                ShopTabButton(
                    label = "ENEMY SKINS",
                    selected = selectedTab == SkinType.ENEMY,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = SkinType.ENEMY }
            }

            val skins = SkinsCatalog.skinsFor(selectedTab)
            // One shared pulse for every swatch instead of each SkinCard
            // running its own rememberInfiniteTransition — same fix as the
            // level-select grid, same reason: many concurrent infinite
            // animation loops add up to visible jank for no visual benefit.
            val swatchPulse = rememberInfiniteTransition(label = "skin_pulse").animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
                label = "skin_pulse_value"
            )
            // Same fix as the level-select grid: LazyVerticalGrid disposes
            // cards as they scroll off screen and rebuilds them fresh when
            // scrolled back into view. Without tracking which skins have
            // already played their pop-in once, every card replayed its
            // full delay+spring entrance animation on every scroll pass —
            // that's what was making this screen stutter while scrolling.
            val seenSkinCards = remember { HashSet<String>() }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(skins) { index, skin ->
                    val unlocked = totalStars >= skin.cost
                    val isEnemyTab = selectedTab == SkinType.ENEMY
                    val cardKey = "$isEnemyTab:${skin.id}"
                    SkinCard(
                        skin = skin,
                        unlocked = unlocked,
                        equipped = unlocked && equippedId(selectedTab) == skin.id,
                        isEnemy = isEnemyTab,
                        index = index,
                        swatchPulse = swatchPulse,
                        alreadyAppeared = cardKey in seenSkinCards,
                        onAppeared = { seenSkinCards.add(cardKey) },
                        onClick = { handleSkinTap(selectedTab, skin) }
                    )
                }
            }
        }

        BannerAd(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

@Composable
private fun ShopTabButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AccentGold.copy(alpha = 0.22f) else CardLocked)
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) AccentGold else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SkinCard(
    skin: Skin,
    unlocked: Boolean,
    equipped: Boolean,
    isEnemy: Boolean,
    index: Int = 0,
    swatchPulse: androidx.compose.runtime.State<Float>,
    alreadyAppeared: Boolean = false,
    onAppeared: () -> Unit = {},
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = when {
        equipped -> AccentAmber
        unlocked -> skin.color.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    // Cards pop in staggered by grid position instead of the whole page of
    // skins appearing at once — same idea as the level-select grid. Only the
    // first time, though: [alreadyAppeared] skips straight to the settled
    // state for a card that's re-entering composition after scrolling back
    // into view, instead of re-running the delay + spring animation again.
    var appeared by remember(skin.id, isEnemy) { mutableStateOf(alreadyAppeared) }
    LaunchedEffect(skin.id, isEnemy) {
        if (!appeared) {
            delay((index.coerceAtMost(15) * 35).toLong())
            appeared = true
            onAppeared()
        }
    }
    val entranceScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.7f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 320f),
        label = "skin_entrance_scale"
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(200),
        label = "skin_entrance_alpha"
    )

    // A little celebratory pop the moment a skin becomes equipped, so
    // choosing a new look feels like an event rather than just a border
    // color silently changing underneath the text.
    val equipPop by animateFloatAsState(
        targetValue = if (equipped) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 380f),
        label = "equip_pop"
    )

    Column(
        modifier = Modifier
            .aspectRatio(0.82f)
            .graphicsLayer {
                val pop = 1f + 0.06f * equipPop
                scaleX = entranceScale * pop
                scaleY = entranceScale * pop
                alpha = entranceAlpha
            }
            .clip(RoundedCornerShape(16.dp))
            .background(CardLocked)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.background(
                        Brush.verticalGradient(listOf(borderColor.copy(alpha = 0.22f + 0.18f * equipPop), Color.Transparent))
                    )
                } else Modifier
            )
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SkinSwatch(skin = skin, dimmed = !unlocked, isEnemy = isEnemy, pulse = swatchPulse)

        Spacer(Modifier.height(8.dp))

        Text(
            text = skin.displayName.uppercase(),
            color = if (unlocked) TextPrimary else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.3.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(Modifier.height(6.dp))

        when {
            equipped -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(11.dp))
                    Text(text = " EQUIPPED", color = AccentAmber, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }
            unlocked -> {
                Text(text = "EQUIP", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                    Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                    Text(text = " ${skin.cost}", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/** A small living preview of the skin color — a soft glowing dot with an
 * accent ring, so the shop card gives a real sense of the in-game hue rather
 * than a flat swatch. Premium skins with [Skin.gradientColors] get a slowly
 * rotating multi-color sweep instead of a flat fill, so they visibly stand
 * out as the fancier tier while still reading clearly at this small size. */
@Composable
private fun SkinSwatch(skin: Skin, dimmed: Boolean, isEnemy: Boolean, pulse: androidx.compose.runtime.State<Float>) {
    val alpha = if (dimmed) 0.35f else 1f
    val gradient = skin.gradientColors
    val glowColor = gradient?.first() ?: skin.color
    val shape = if (isEnemy) RoundedCornerShape(6.dp) else CircleShape

    val sweepAngle = if (gradient != null) {
        val infinite = rememberInfiniteTransition(label = "skin_sweep")
        val angle by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sweep_angle"
        )
        angle
    } else 0f

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .drawWithCache {
                    onDrawBehind {
                        drawCircle(glowColor.copy(alpha = 0.25f * alpha * pulse.value))
                    }
                }
        )
        if (gradient != null) {
            // Fancy skins: clip to the token shape first, then paint a slowly
            // rotating sweep gradient across it — reads as a genuinely
            // multi-color premium finish rather than a flat single hue.
            Box(
                modifier = Modifier
                    .size(if (isEnemy) 26.dp else 24.dp)
                    .clip(shape)
                    .drawWithCache {
                        onDrawBehind {
                            rotate(sweepAngle) {
                                drawRect(brush = Brush.sweepGradient(gradient.map { it.copy(alpha = alpha) }))
                            }
                        }
                    }
            )
        } else {
            Box(
                modifier = Modifier
                    .size(if (isEnemy) 26.dp else 24.dp)
                    .background(skin.color.copy(alpha = alpha), shape)
            )
        }
    }
}
