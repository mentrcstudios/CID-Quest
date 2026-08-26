package com.mentricstudios.cidquest.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Update
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ui.theme.AccentOrange
import com.mentricstudios.cidquest.ui.theme.AccentTeal
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.bounceClick

/**
 * "Terms of Service / Privacy Policy" viewer, shown from the Terms and Age
 * Gate screens. Scrollable so the full copy is reachable on small screens
 * without clipping.
 */
@Composable
fun LegalDocNotice(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val closeInteraction = remember { MutableInteractionSource() }

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
            enter = fadeIn(tween(200)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 320f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.75f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BackgroundBottom)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(AccentTeal.copy(alpha = 0.4f), AccentOrange.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "  TERMS & PRIVACY",
                        color = AccentOrange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .bounceClick(closeInteraction, pressedScale = 0.85f, playSound = false)
                            .clip(CircleShape)
                            .background(TextSecondary.copy(alpha = 0.14f))
                            .clickable(
                                interactionSource = closeInteraction,
                                indication = null,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 14.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentTeal.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    LegalSection(
                        icon = Icons.Filled.Gavel,
                        title = "Using CID Quest",
                        body = "CID Quest is provided for personal, non-commercial entertainment. " +
                            "By playing, you agree not to modify, reverse-engineer, or redistribute the app, " +
                            "and to use it in line with applicable local laws."
                    )
                    LegalSection(
                        icon = Icons.Filled.SavedSearch,
                        title = "Your progress",
                        body = "Level completion, star ratings, and unlocked skins are stored only on this " +
                            "device. Uninstalling the app or clearing its storage erases this progress permanently."
                    )
                    LegalSection(
                        icon = Icons.Filled.Campaign,
                        title = "Advertising",
                        body = "This app shows ads served by Google AdMob (banner, interstitial, and " +
                            "optional rewarded ads for bonus hints) to keep the game free. AdMob and its " +
                            "partners may collect an advertising identifier and other technical data to serve " +
                            "and measure ads, subject to their own privacy policies."
                    )
                    LegalSection(
                        icon = Icons.Filled.Shield,
                        title = "Data we collect",
                        body = "Beyond what the ad network collects for ad delivery, CID Quest does not " +
                            "require an account, does not collect personal information, and does not send " +
                            "your gameplay data to our own servers. Play reminder notifications (if enabled) " +
                            "are scheduled entirely on this device."
                    )
                    LegalSection(
                        icon = Icons.Filled.Update,
                        title = "Changes",
                        body = "These terms may be updated in future app updates. Continued use after an " +
                            "update means you accept the revised terms."
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Tap outside this card to close.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LegalSection(icon: ImageVector, title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentTeal,
                modifier = Modifier.size(15.dp)
            )
            Text(
                "  $title",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            body,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(start = 21.dp)
        )
    }
}
