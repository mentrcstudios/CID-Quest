package com.mentricstudios.cidquest.screens

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
import androidx.compose.foundation.border
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
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.SettingsPrefs
import com.mentricstudios.cidquest.util.SoundManager
import com.mentricstudios.cidquest.util.bounceClick

/**
 * Deliberately plain: flat solid background, no gradients, no glow/blur
 * effects, no breathing/pulsing animation on the logo, square-cornered
 * blocky buttons instead of rounded pill shapes. This is on purpose — a
 * cheap, low-budget look that also renders cheaply (nothing here runs a
 * continuous per-frame animation), so it performs fine on low-end phones.
 */
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
            .background(BackgroundTop)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_cid_logo),
                contentDescription = "Cid Quest logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .border(width = 2.dp, color = AccentGold)
            )

            PlayButton(
                modifier = Modifier.padding(top = 28.dp),
                onClick = onPlay
            )
        }

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
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun PlayButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(width = 180.dp, height = 56.dp)
            .background(AccentGold)
            .border(width = 2.dp, color = TextPrimary)
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = BackgroundTop, modifier = Modifier.size(22.dp))
            Text(
                text = "PLAY",
                color = BackgroundTop,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun BottomIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(CardLocked)
            .border(width = 1.dp, color = TextSecondary)
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(22.dp))
    }
}
