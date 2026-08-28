package com.mentricstudios.cidquest.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.bounceClick

/**
 * Simple, no-fuss age check — one tap, no birthdate picker, no entrance
 * animation. This is a private/personal build, not a store release, so
 * this exists purely as a lighthearted "grown-ups only" gate rather than a
 * legally-worded consent flow.
 */
@Composable
fun AgeGateScreen(onConfirmed: () -> Unit) {
    var showNopeMessage by remember { mutableStateOf(false) }

    val yesInteraction = remember { MutableInteractionSource() }
    val noInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundTop),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .background(CardLocked)
                .border(width = 2.dp, color = AccentGold)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Quick check before you dive in",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Are you 18 or older?",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(BackgroundTop)
                        .border(width = 1.dp, color = TextSecondary)
                        .bounceClick(noInteraction)
                        .clickable(interactionSource = noInteraction, indication = null) {
                            showNopeMessage = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "NOPE", fontWeight = FontWeight.Bold, color = TextSecondary)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(AccentGold)
                        .border(width = 1.dp, color = TextPrimary)
                        .bounceClick(yesInteraction)
                        .clickable(interactionSource = yesInteraction, indication = null) {
                            onConfirmed()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "YES, I'M 18+", fontWeight = FontWeight.Bold, color = BackgroundTop)
                }
            }

            if (showNopeMessage) {
                Text(
                    text = "Come back when you're older — see ya!",
                    color = AccentGold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }
    }
}
