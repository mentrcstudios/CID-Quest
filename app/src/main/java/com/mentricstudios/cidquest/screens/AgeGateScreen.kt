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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ui.theme.AccentAmber
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.bounceClick

/**
 * Simple, no-fuss age check — one tap, no birthdate picker. This is a
 * private/personal build, not a store release, so this exists purely as a
 * lighthearted "grown-ups only" gate rather than a legally-worded consent
 * flow.
 */
@Composable
fun AgeGateScreen(onConfirmed: () -> Unit) {
    var cardVisible by remember { mutableStateOf(false) }
    var showNopeMessage by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }

    val yesInteraction = remember { MutableInteractionSource() }
    val noInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = cardVisible,
            enter = fadeIn(tween(260)) + scaleIn(
                initialScale = 0.9f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(BackgroundBottom)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(AccentGold.copy(alpha = 0.5f), AccentAmber.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AccentGold.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SentimentVerySatisfied,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "Quick check before you dive in",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 14.dp)
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
                            .height(52.dp)
                            .bounceClick(noInteraction)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardLocked)
                            .clickable(interactionSource = noInteraction, indication = null) {
                                showNopeMessage = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "NOPE", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .bounceClick(yesInteraction)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(AccentGold, AccentGold.copy(alpha = 0.7f)))
                            )
                            .clickable(interactionSource = yesInteraction, indication = null) {
                                onConfirmed()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "YES, I'M 18+", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = BackgroundTop)
                    }
                }

                AnimatedVisibility(visible = showNopeMessage) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Block,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Come back when you're older — see ya!",
                            color = AccentAmber,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
