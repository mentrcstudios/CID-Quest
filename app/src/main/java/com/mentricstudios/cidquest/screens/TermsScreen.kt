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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ui.theme.AccentOrange
import com.mentricstudios.cidquest.ui.theme.AccentTeal
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.bounceClick

@Composable
fun TermsScreen(onAgree: () -> Unit) {
    var showLegalDoc by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }

    val agreeInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))),
        contentAlignment = Alignment.Center
    ) {
        // Dimmed background title, mimicking the app behind the dialog
        Text(
            text = "CID QUEST",
            color = TextSecondary.copy(alpha = 0.15f),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.Center)
        )

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
                            listOf(AccentTeal.copy(alpha = 0.5f), AccentOrange.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentTeal.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Gavel,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Terms of Service & Privacy Policy",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 14.dp)
                )

                Text(
                    text = "Before you continue, please review and accept our app Terms " +
                        "of Service and Privacy Policy.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                )

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Terms of Service",
                        color = AccentTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showLegalDoc = true }
                    )
                    Text(text = "   ·   ", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        text = "Privacy Policy",
                        color = AccentTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showLegalDoc = true }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .bounceClick(agreeInteraction)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(AccentTeal, AccentTeal.copy(alpha = 0.7f)))
                            )
                            .clickable(interactionSource = agreeInteraction, indication = null, onClick = onAgree)
                            .padding(horizontal = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = BackgroundTop,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "  AGREE",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = BackgroundTop
                            )
                        }
                    }
                }
            }
        }

        if (showLegalDoc) {
            LegalDocNotice(onDismiss = { showLegalDoc = false })
        }
    }
}
