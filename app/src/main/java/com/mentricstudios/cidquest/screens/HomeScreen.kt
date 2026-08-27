package com.mentricstudios.cidquest.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.mentricstudios.cidquest.R
import com.mentricstudios.cidquest.ads.BannerAd
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.SettingsPrefs
import com.mentricstudios.cidquest.util.SoundManager
import com.mentricstudios.cidquest.util.bounceClick

@Composable
fun HomeScreen(
    onPlay: () -> Unit,
    onSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var soundEnabled by remember { mutableStateOf(SettingsPrefs.isSoundEnabled(context)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        // Logo + Play button, truly centered on the full screen
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeLogoMark()

            PlayButton(
                modifier = Modifier.padding(top = 32.dp),
                onClick = onPlay
            )
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

    Box(modifier = modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer { scaleX = breathe; scaleY = breathe }
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = glowAlpha))
        )
        Image(
            painter = painterResource(id = R.drawable.img_cid_logo),
            contentDescription = "Cid Quest logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(200.dp)
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
