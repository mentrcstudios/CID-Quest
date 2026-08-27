package com.mentricstudios.cidquest.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

/**
 * Adds a gentle "press down" scale animation to any clickable composable,
 * plus (by default) a short click SFX on press. Usage:
 *   val interactionSource = remember { MutableInteractionSource() }
 *   Box(
 *       modifier = Modifier
 *           .bounceClick(interactionSource)
 *           .clickable(interactionSource = interactionSource, indication = null) { onClick() }
 *   )
 *
 * [playSound] defaults to true so every menu/UI button gets the click SFX
 * for free. Core in-maze controls (D-pad, pause/restart, hints) pass
 * `playSound = false` — those fire rapidly during play and a click on every
 * tap would get noisy fast rather than feel like feedback.
 */
fun Modifier.bounceClick(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.92f,
    playSound: Boolean = true
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
        label = "bounce_scale"
    )
    val context = LocalContext.current
    LaunchedEffect(isPressed) {
        if (isPressed && playSound) {
            SoundManager.playClick(context)
        }
    }
    this.graphicsLayer(scaleX = scale, scaleY = scale)
}
