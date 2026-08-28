package com.mentricstudios.cidquest.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.SoundManager
import kotlinx.coroutines.delay

private val LOADING_MESSAGES = listOf(
    "Carving corridors (badly)…",
    "Hiding a few dead ends, teehee…",
    "Bribing the guards to look busy…",
    "Making sure the exit isn't a trap…",
    "Almost ready, hang tight…"
)

/**
 * Plain flat splash — solid background, a static title, a plain progress
 * bar (no gradient fill), and swapped-not-animated messages. No looping
 * animations running here at all, on purpose: this is the very first thing
 * a low-end phone has to render, so it should cost as close to nothing as
 * possible.
 */
@Composable
fun LoadingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var progressStep by remember { mutableIntStateOf(0) }
    var messageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        SoundManager.playLoadingMusic(context)
        for (step in 1..LOADING_MESSAGES.size) {
            messageIndex = step - 1
            delay(420)
            progressStep = step
        }
        delay(250)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundTop),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "CID QUEST",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth(0.6f)
                    .height(10.dp)
                    .border(width = 1.dp, color = TextSecondary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressStep / LOADING_MESSAGES.size.toFloat())
                        .height(10.dp)
                        .background(AccentGold)
                )
            }

            Text(
                text = LOADING_MESSAGES[messageIndex],
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
