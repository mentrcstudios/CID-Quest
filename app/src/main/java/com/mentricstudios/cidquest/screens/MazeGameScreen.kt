package com.mentricstudios.cidquest.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import com.mentricstudios.cidquest.R
import com.mentricstudios.cidquest.ads.AdFrequency
import com.mentricstudios.cidquest.ads.AdsManager
import com.mentricstudios.cidquest.game.CellPos
import com.mentricstudios.cidquest.game.Direction
import com.mentricstudios.cidquest.game.MazeGenerator
import com.mentricstudios.cidquest.game.MazeGrid
import com.mentricstudios.cidquest.game.MazeLevel
import com.mentricstudios.cidquest.game.MazeLevels
import com.mentricstudios.cidquest.ui.theme.AccentAmber
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.CategoryEnemies
import com.mentricstudios.cidquest.ui.theme.EnemyColor
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.CharacterPhoto
import com.mentricstudios.cidquest.util.GameProgress
import com.mentricstudios.cidquest.util.OnboardingPrefs
import com.mentricstudios.cidquest.util.SettingsPrefs
import com.mentricstudios.cidquest.util.SoundManager
import com.mentricstudios.cidquest.util.bounceClick
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.random.Random

/**
 * A wall endpoint's position after the cell's tiny inward "settle" animation
 * (walls start a touch shorter and snap out to full length as the reveal
 * wave passes over them). Pulled out to a plain top-level function — same
 * math as before — instead of a local closure defined inside the board's
 * per-cell draw loop, since that closure was a fresh allocation for every
 * single cell on every single animation frame.
 */
private fun settledOffset(x: Float, y: Float, cx: Float, cy: Float, settle: Float): Offset =
    Offset(cx + (x - cx) * settle, cy + (y - cy) * settle)

/** A guard's current behavior — see the state machine in [MazeGameScreen] where enemies are animated. */
private enum class EnemyAiState { PATROL, CHASE, RETURNING }

@Composable
fun MazeGameScreen(
    category: String,
    levelNumber: Int,
    onHome: () -> Unit,
    onNextLevel: (Int) -> Unit = {}
) {
    val level = remember(category, levelNumber) { MazeLevels.find(category, levelNumber) }
    val context = LocalContext.current
    val activity = context as? Activity
    val nextLevelNumber = levelNumber + 1
    val hasNextLevel = remember(category, levelNumber) {
        MazeLevels.find(category, nextLevelNumber) != null
    }

    // Every "leave this level" action (home, or advance to the next maze)
    // funnels through here so the interstitial — capped to roughly every
    // other transition by AdFrequency, so it's never on literally every tap
    // — has exactly one place to hook into instead of being duplicated at
    // every call site.
    //
    // The slot is only marked "used" when an interstitial actually shows.
    // If this transition was eligible but nothing was preloaded in time, we
    // don't burn the slot — the very next transition gets another shot
    // instead of waiting a further SHOW_EVERY turns for one that was never
    // actually delivered.
    fun goHome() {
        val eligible = activity != null && AdFrequency.isInterstitialEligible(context)
        val ready = eligible && AdsManager.isInterstitialReady()
        if (ready) {
            AdsManager.showInterstitial(activity!!) {
                AdFrequency.recordTransition(context, interstitialShown = true)
                onHome()
            }
        } else {
            AdFrequency.recordTransition(context, interstitialShown = false)
            onHome()
        }
    }
    fun goNextLevel() {
        val eligible = activity != null && AdFrequency.isInterstitialEligible(context)
        val ready = eligible && AdsManager.isInterstitialReady()
        if (ready) {
            AdsManager.showInterstitial(activity!!) {
                AdFrequency.recordTransition(context, interstitialShown = true)
                onNextLevel(nextLevelNumber)
            }
        } else {
            AdFrequency.recordTransition(context, interstitialShown = false)
            onNextLevel(nextLevelNumber)
        }
    }

    if (level == null) {
        // Defensive fallback — navigation should never send us here without a valid level.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))),
            contentAlignment = Alignment.Center
        ) {
            Text("Level not available yet.", color = TextSecondary)
        }
        return
    }

    val grid = remember(level) { level.buildGrid() }
    val optimalMoves = remember(level, grid) { level.optimalMoveCount(grid) }
    val haptics = LocalHapticFeedback.current
    var vibrationEnabled by remember { mutableStateOf(SettingsPrefs.isVibrationEnabled(context)) }
    var onScreenControlsEnabled by remember { mutableStateOf(SettingsPrefs.isOnScreenControlsEnabled(context)) }
    // No more purchasable skins — the hero/guard tokens always render in
    // these two fixed colors.
    val playerSkinColor = AccentGold
    val enemySkinColor = EnemyColor

    // Character photos: the player's own picked photo (or the bundled
    // default) and the three fixed guard photos — decoded, cropped, and
    // circle-masked once per process and cached in CharacterPhoto, not
    // redone on every level transition.
    val playerPhoto = remember { CharacterPhoto.playerAvatar(context) }
    val enemyPhotos = remember { CharacterPhoto.enemyAvatars(context) }
    fun vibrate(type: HapticFeedbackType) {
        if (vibrationEnabled) haptics.performHapticFeedback(type)
    }

    // Heartbeat loop plays for the life of this screen whenever the level has
    // patrol guards — keyed off the level's actual enemy list.
    val hasEnemyThreat = remember(category, level) { level.enemies.isNotEmpty() }
    DisposableEffect(hasEnemyThreat) {
        // Capture the ownership token from this specific start call so our
        // onDispose can only stop the track if nobody newer has taken over
        // it in the meantime (see SoundManager.playToken for why this
        // matters — it's what fixes the intermittent "no heartbeat" bug).
        val myToken = if (hasEnemyThreat) SoundManager.startBackgroundMusic(context) else null
        onDispose {
            SoundManager.stopBackgroundMusic(myToken)
            SoundManager.stopAllOneShots()
        }
    }

    // Accent used for the goal ring / entrance stub / win particles.
    val categoryAccent = CategoryEnemies

    // Distance of every cell from the entrance, used to sweep the maze into view
    // as an outward "ink" wave rather than popping in all at once.
    val distanceFromStart = remember(level) { MazeGenerator.distancesFrom(grid, level.start) }
    val maxDistance = remember(level) {
        distanceFromStart.maxOf { row -> row.maxOrNull() ?: 0 }.coerceAtLeast(1)
    }
    // 0f..1f — drives the wave-reveal of walls/corridors across the grid.
    val gridReveal = remember(level) { Animatable(0f) }
    // 0f..1f — drives the player dot's bounce-in once the wave reaches the start cell.
    val playerEntrance = remember(level) { Animatable(0f) }

    val path = remember(level) { mutableStateListOf(level.start) }
    var moveCount by remember(level) { mutableStateOf(0) }
    var hintsRemaining by remember(level) { mutableStateOf(level.maxHints) }
    var hintsUsed by remember(level) { mutableStateOf(0) }
    // Only the very first hint on a level is free. Every hint after that —
    // including the 2nd hint on levels with a 2-hint budget — requires
    // watching a rewarded ad, same as the "out of hints" ad offer below.
    var freeHintUsed by remember(level) { mutableStateOf(false) }
    // "Guiding Light" hint — instead of flashing a single next-step arrow for
    // under two seconds (which players kept finding useless at junctions),
    // one hint now lights up a short stretch of the real route ahead (up to
    // 4 cells), with a little spark that visibly travels along it.
    var hintPath by remember(level) { mutableStateOf<List<CellPos>>(emptyList()) }
    var hintTick by remember(level) { mutableStateOf(0) }
    var watchingHintAd by remember(level) { mutableStateOf(false) }
    val hintTravel = remember(level) { Animatable(0f) }
    val hintFade = remember(level) { Animatable(0f) }
    var isPaused by remember(level) { mutableStateOf(false) }
    LaunchedEffect(isPaused) {
        if (isPaused) SoundManager.pauseBackgroundMusic() else SoundManager.resumeBackgroundMusic()
    }
    // First-ever level a new player opens gets a short coach-mark walkthrough
    // (swipe to move, use hints, pause/restart, star scoring) instead of
    // dropping them straight into the maze with zero explanation. Shown at
    // most once per install, tracked in OnboardingPrefs, independent of the
    // Terms/Age onboarding flag.
    var showTutorial by remember(level) { mutableStateOf(false) }
    var isWon by remember(level) { mutableStateOf(false) }
    var showWinOverlay by remember(level) { mutableStateOf(false) }
    var showLockedDialog by remember { mutableStateOf(false) }
    var showRetryConfirm by remember { mutableStateOf(false) }
    var elapsedSeconds by remember(level) { mutableStateOf(0) }
    var invalidBumpTick by remember(level) { mutableStateOf(0) }

    // Smoothly-animated player position, in fractional cell coordinates.
    val animRow = remember(level) { Animatable(level.start.row.toFloat()) }
    val animCol = remember(level) { Animatable(level.start.col.toFloat()) }
    // A quick "no" wiggle when the player bumps into a wall.
    val shake = remember(level) { Animatable(0f) }
    // Drives the goal-reached particle burst, 0f (idle) -> 1f (finished).
    val celebration = remember(level) { Animatable(0f) }

    // --- Enemies category: fixed-path patrol guards ---------------------
    // Each guard's actual walkable route is derived once from the grid via
    // BFS shortest-path, then the guard ping-pongs back and forth along it
    // forever while on ordinary patrol (see the AI state machine below for
    // what happens once it spots the player).
    val enemyPaths = remember(level, grid) {
        level.enemies.map { enemy -> MazeGenerator.shortestPath(grid, enemy.from, enemy.to) }
    }
    val enemyAnimRows = remember(level) { level.enemies.map { Animatable(it.from.row.toFloat()) } }
    val enemyAnimCols = remember(level) { level.enemies.map { Animatable(it.from.col.toFloat()) } }
    // Which way each guard is currently facing — drives the eyes so they
    // visibly "look" the direction they're walking instead of staring blankly.
    val enemyDirections = remember(level) {
        mutableStateListOf(*Array(level.enemies.size) { Direction.SOUTH })
    }
    // Shared across every guard's coroutine below — deliberately NOT
    // per-guard state. With several guards on screen, each independently
    // tracking its own cooldown meant they could each fire the same cue
    // within moments of one another (2-3 guards near the player = 2-3
    // overlapping ambient/chase sounds back to back), which is exactly
    // what "sounds repeat too much" was. One shared clock per cue type
    // means only one plays at a time, no matter how many guards trigger it.
    var lastAmbientPlayedAt by remember(level) { mutableStateOf(0L) }
    var lastChaseStartPlayedAt by remember(level) { mutableStateOf(0L) }
    var lastEvadedPlayedAt by remember(level) { mutableStateOf(0L) }
    // Bumped every time the level resets so enemy-patrol coroutines restart
    // cleanly from their spawn point instead of continuing mid-stride.
    var resetTick by remember(level) { mutableStateOf(0) }
    var isCaught by remember(level) { mutableStateOf(false) }

    // A guard's coroutine getting cancelled (resetTick changing) and a
    // synchronous SoundManager.play___() call it was already mid-way
    // through executing can race — cooperative cancellation only takes
    // effect at the next suspension point, so a stray sound can occasionally
    // slip out in that same instant even though stopAllOneShots() below
    // already ran. This sweep fires shortly after every reset and silences
    // anything that snuck through — the actual fix for "old sound effects
    // still there after restart."
    LaunchedEffect(resetTick) {
        if (resetTick > 0) {
            delay(120)
            SoundManager.stopAllOneShots()
        }
    }

    fun resetLevel() {
        path.clear()
        path.add(level.start)
        moveCount = 0
        hintsRemaining = level.maxHints
        hintsUsed = 0
        freeHintUsed = false
        hintPath = emptyList()
        isWon = false
        showWinOverlay = false
        isPaused = false
        elapsedSeconds = 0
        isCaught = false
        resetTick++
        SoundManager.stopAllOneShots()
        if (hasEnemyThreat) SoundManager.resumeBackgroundMusic()
    }

    fun tryMove(dir: Direction): Boolean {
        if (isWon || isPaused || isCaught || showTutorial || playerEntrance.value < 0.3f) return false
        val current = path.last()

        if (path.size >= 2 && path[path.size - 2] == current.step(dir)) {
            path.removeAt(path.size - 1)
            moveCount++
            vibrate(HapticFeedbackType.TextHandleMove)
            return true
        }
        if (grid.canMove(current, dir)) {
            val next = current.step(dir)
            path.add(next)
            moveCount++
            vibrate(HapticFeedbackType.TextHandleMove)
            if (next == level.goal) {
                isWon = true
                SoundManager.pauseBackgroundMusic()
                vibrate(HapticFeedbackType.LongPress)
            }
            return true
        }
        invalidBumpTick++
        vibrate(HapticFeedbackType.LongPress)
        return false
    }

    fun useHint() {
        if (hintsRemaining <= 0 || isWon || isPaused || isCaught || showTutorial) return
        val remaining = MazeGenerator.shortestPath(grid, path.last(), level.goal)
        if (remaining.size >= 2) {
            // Reveal up to 4 real steps ahead (fewer if the goal is closer than
            // that) so a single hint actually carries the player through a
            // junction or two, instead of one arrow that expired before they
            // could act on it.
            val revealSteps = (remaining.size - 1).coerceAtMost(4)
            hintPath = remaining.take(revealSteps + 1)
            hintTick++
            hintsRemaining--
            hintsUsed++
            vibrate(HapticFeedbackType.TextHandleMove)
        }
    }

    // Level intro: the maze sweeps into view cell-by-cell as an outward wave from
    // the entrance, then the player pops in with a springy overshoot bounce.
    LaunchedEffect(level) {
        gridReveal.snapTo(0f)
        playerEntrance.snapTo(0f)
        val waveDuration = (280 + maxDistance * 55).coerceIn(450, 1400)
        gridReveal.animateTo(1f, tween(waveDuration, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
        playerEntrance.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 260f))
        if (!OnboardingPrefs.hasSeenGameTutorial(context)) {
            showTutorial = true
        }
    }

    // On a fresh level load / retry-reset, snap the player straight back to
    // the start cell instead of gliding there. Without this, the dot would
    // spring back across the board over several frames, and if a guard was
    // anywhere near that path it could re-trigger a catch immediately after
    // the "CAUGHT!" overlay had just cleared — making it look stuck/looping.
    LaunchedEffect(level, resetTick) {
        animRow.snapTo(level.start.row.toFloat())
        animCol.snapTo(level.start.col.toFloat())
    }

    // Glide the player dot to wherever the path currently ends.
    LaunchedEffect(level, path.size) {
        val target = path.last()
        val spec = spring<Float>(dampingRatio = 0.62f, stiffness = 380f)
        animRow.animateTo(target.row.toFloat(), spec)
    }
    LaunchedEffect(level, path.size) {
        val target = path.last()
        val spec = spring<Float>(dampingRatio = 0.62f, stiffness = 380f)
        animCol.animateTo(target.col.toFloat(), spec)
    }

    // Wall-bump wiggle.
    LaunchedEffect(invalidBumpTick) {
        if (invalidBumpTick > 0) {
            shake.snapTo(0f)
            shake.animateTo(1f, tween(55))
            shake.animateTo(-1f, tween(85))
            shake.animateTo(0f, tween(70))
        }
    }

    // Guiding-light trail: a spark travels the length of the revealed
    // stretch, then the whole trail lingers a moment longer (so the player
    // has time to actually look at the route) before softly fading out.
    // Keyed on hintTick so tapping hint again mid-animation always restarts
    // cleanly rather than reusing a stale countdown.
    LaunchedEffect(hintPath, hintTick) {
        if (hintPath.size >= 2) {
            hintFade.snapTo(1f)
            hintTravel.snapTo(0f)
            val travelMillis = (170 * (hintPath.size - 1)).coerceIn(220, 900)
            hintTravel.animateTo(1f, tween(travelMillis, easing = LinearEasing))
            delay(1200)
            hintFade.animateTo(0f, tween(450))
            hintPath = emptyList()
        }
    }

    // Goal-reached celebration burst, then reveal the win card.
    LaunchedEffect(isWon) {
        if (isWon) {
            SoundManager.playReward(context)
            celebration.snapTo(0f)
            celebration.animateTo(1f, tween(850, easing = LinearEasing))
            showWinOverlay = true
        } else {
            celebration.snapTo(0f)
        }
    }

    // Simple elapsed-time counter. Runs one continuous loop for the whole
    // level attempt instead of restarting on every pause/resume — restarting
    // used to swallow up to a full second of progress each time the player
    // paused, making the displayed time drift from the real elapsed time.
    LaunchedEffect(level) {
        while (true) {
            delay(1000)
            if (!isPaused && !isWon && !isCaught && !showTutorial) elapsedSeconds++
        }
    }

    // Each guard runs a small state machine instead of blindly ping-ponging
    // forever:
    //   PATROL   — follows its fixed route, same as before. While patrolling,
    //              a wide-radius "something's nearby" cue plays occasionally
    //              (no behavior change yet — just atmosphere/tension).
    //   CHASE    — triggered by a tighter detection radius: the guard
    //              abandons its route and live-pathfinds toward the
    //              player's current cell every step (via the same
    //              MazeGenerator.shortestPath used for hints), a notch
    //              faster than patrol speed.
    //   RETURNING— once the player has stayed clear of the guard for a full
    //              5 seconds, the guard "loses" them: a brief pause (like a
    //              person stopping to look around before giving up), a
    //              relieved cue, then it paths back to where it left its
    //              route and resumes patrolling from there.
    // Re-detection at any point during CHASE/RETURNING immediately resets
    // the clock rather than waiting for the guard to fully give up first.
    level.enemies.forEachIndexed { index, enemy ->
        LaunchedEffect(level, index, resetTick) {
            val route = enemyPaths.getOrNull(index) ?: return@LaunchedEffect
            enemyAnimRows[index].snapTo(enemy.from.row.toFloat())
            enemyAnimCols[index].snapTo(enemy.from.col.toFloat())
            if (route.size < 2) return@LaunchedEffect

            while (playerEntrance.value < 1f) delay(16)
            // Guards can't enter CHASE during this window no matter how close
            // they start to the player's spawn cell — without this, a guard
            // whose patrol route starts adjacent to the entrance (e.g. level
            // 2's guard at (1,1) vs. the player's (0,0) spawn) would detect
            // and beeline the player before they'd had a single frame to
            // react. This is exactly what "can't move at all, guard catches
            // me instantly" was.
            val spawnGraceUntil = System.currentTimeMillis() + 900L

            var routeIndex = 0
            var forward = true
            var state = EnemyAiState.PATROL
            var lastSeenAtMillis = 0L
            var wasAmbientNear = false
            val chaseRadius = 1.15f
            val ambientRadius = 1.8f
            val ambientCooldownMillis = 1600L
            val chaseStartCooldownMillis = 800L
            val evadedCooldownMillis = 800L
            val escapeGraceMillis = 5000L

            fun currentGuardCell() = CellPos(
                enemyAnimRows[index].value.roundToInt(),
                enemyAnimCols[index].value.roundToInt()
            )
            fun currentPlayerCell() = CellPos(animRow.value.roundToInt(), animCol.value.roundToInt())
            fun distanceToPlayer(): Pair<Float, Float> {
                val dRow = abs(animRow.value - enemyAnimRows[index].value)
                val dCol = abs(animCol.value - enemyAnimCols[index].value)
                return dRow to dCol
            }
            suspend fun glideTo(target: CellPos, from: CellPos, stepMillis: Int) {
                val dir = Direction.values().firstOrNull { from.step(it) == target }
                if (dir != null) enemyDirections[index] = dir
                val spec = tween<Float>(stepMillis, easing = LinearEasing)
                coroutineScope {
                    launch { enemyAnimRows[index].animateTo(target.row.toFloat(), spec) }
                    launch { enemyAnimCols[index].animateTo(target.col.toFloat(), spec) }
                }
            }

            while (true) {
                while (isPaused || isWon || isCaught || showTutorial) delay(80)
                val (dRow, dCol) = distanceToPlayer()
                val chaseNear = dRow < chaseRadius && dCol < chaseRadius
                val ambientNear = dRow < ambientRadius && dCol < ambientRadius
                val now = System.currentTimeMillis()
                val graceActive = now < spawnGraceUntil

                when (state) {
                    EnemyAiState.PATROL -> {
                        if (chaseNear && !graceActive) {
                            state = EnemyAiState.CHASE
                            lastSeenAtMillis = now
                            if (now - lastChaseStartPlayedAt >= chaseStartCooldownMillis) {
                                SoundManager.playChaseStart(context)
                                lastChaseStartPlayedAt = now
                            }
                        } else {
                            if (ambientNear && !wasAmbientNear && now - lastAmbientPlayedAt >= ambientCooldownMillis) {
                                SoundManager.playSpotted(context)
                                lastAmbientPlayedAt = now
                            }
                            wasAmbientNear = ambientNear
                            val prevIndex = routeIndex
                            routeIndex = if (forward) routeIndex + 1 else routeIndex - 1
                            if (routeIndex >= route.size - 1) {
                                routeIndex = route.size - 1
                                forward = false
                            } else if (routeIndex <= 0) {
                                routeIndex = 0
                                forward = true
                            }
                            glideTo(route[routeIndex], route[prevIndex], enemy.stepMillis)
                        }
                    }
                    EnemyAiState.CHASE -> {
                        if (chaseNear) {
                            lastSeenAtMillis = now
                        } else if (now - lastSeenAtMillis >= escapeGraceMillis) {
                            state = EnemyAiState.RETURNING
                            if (now - lastEvadedPlayedAt >= evadedCooldownMillis) {
                                SoundManager.playEvaded(context)
                                lastEvadedPlayedAt = now
                            }
                            delay(450) // a beat of "huh, where'd they go" before heading back
                        }
                        if (state == EnemyAiState.CHASE) {
                            val guardCell = currentGuardCell()
                            val chasePath = MazeGenerator.shortestPath(grid, guardCell, currentPlayerCell())
                            val target = chasePath.getOrNull(1) ?: guardCell
                            if (target != guardCell) {
                                val chaseStepMillis = (enemy.stepMillis * 0.8f).toInt().coerceAtLeast(120)
                                glideTo(target, guardCell, chaseStepMillis)
                            } else {
                                delay(80)
                            }
                        }
                    }
                    EnemyAiState.RETURNING -> {
                        if (chaseNear) {
                            state = EnemyAiState.CHASE
                            lastSeenAtMillis = now
                            if (now - lastChaseStartPlayedAt >= chaseStartCooldownMillis) {
                                SoundManager.playChaseStart(context)
                                lastChaseStartPlayedAt = now
                            }
                        } else {
                            val guardCell = currentGuardCell()
                            val homeCell = route[routeIndex]
                            if (guardCell == homeCell) {
                                state = EnemyAiState.PATROL
                                wasAmbientNear = false
                            } else {
                                val backPath = MazeGenerator.shortestPath(grid, guardCell, homeCell)
                                val target = backPath.getOrNull(1) ?: homeCell
                                if (target != guardCell) {
                                    glideTo(target, guardCell, enemy.stepMillis)
                                } else {
                                    delay(80)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Grid/AABB collision: every tick, compare the player's and each guard's
    // fractional-cell centers as unit-size axis-aligned boxes. Any overlap
    // on both axes counts as a hit, however the guard reached that cell.
    LaunchedEffect(level, resetTick) {
        if (level.enemies.isEmpty()) return@LaunchedEffect
        // Short invulnerability window right after spawn/reset so a guard
        // that happens to be near the start cell can't immediately re-catch
        // the player before everything has visually settled into place.
        delay(350)
        while (true) {
            delay(16)
            if (isPaused || isWon || isCaught || showTutorial || playerEntrance.value < 1f) continue
            val hit = level.enemies.indices.any { idx ->
                val dRow = abs(animRow.value - enemyAnimRows[idx].value)
                val dCol = abs(animCol.value - enemyAnimCols[idx].value)
                dRow < 0.62f && dCol < 0.62f
            }
            if (hit) {
                isCaught = true
                SoundManager.pauseBackgroundMusic()
                // SoundManager.playWrong already includes its own strong
                // vibration (gated on the Settings toggle), so the lighter
                // haptics.performHapticFeedback pulse below is skipped here
                // to avoid stacking two vibrations on the same catch.
                SoundManager.playWrong(context)
                SoundManager.playGameOver(context)
                // Once caught, this loop's job is done for this life — stop
                // polling so it can't flag another hit while the overlay is
                // showing and resetLevel() is about to cancel/relaunch us.
                return@LaunchedEffect
            }
        }
    }

    // Caught by a guard — the Game Over overlay takes over from here and
    // gives the player explicit RETRY / HOME buttons instead of silently
    // auto-resetting, so they're never caught off guard by a level suddenly
    // restarting itself.

    // A clean run — no hints, no wasted moves — just for the win-screen
    // flavor text/badge. Purely a per-attempt nicety now, nothing is scored
    // or saved beyond whether the level has been cleared at all.
    val perfectRun = isWon && hintsUsed == 0 && moveCount.coerceAtLeast(1) <= optimalMoves

    LaunchedEffect(isWon) {
        if (isWon) {
            GameProgress.markCompleted(context, category, levelNumber)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 32.dp)
        ) {
            GameTopBar(
                title = "${category.uppercase()} - $levelNumber",
                hintsRemaining = hintsRemaining,
                hintsEnabled = !isWon && !isPaused && !isCaught && !watchingHintAd &&
                    ((!freeHintUsed && hintsRemaining > 0) || (activity != null && AdsManager.isRewardedReady())),
                onPause = {
                    if (!isWon && !isCaught) {
                        isPaused = true
                        SoundManager.pauseBackgroundMusic()
                    }
                },
                onRestart = { if (!isWon && !isCaught) showRetryConfirm = true },
                onHint = {
                    when {
                        // First hint of the attempt is free.
                        !freeHintUsed && hintsRemaining > 0 -> {
                            freeHintUsed = true
                            useHint()
                        }
                        // Still within this level's hint budget, but the free
                        // one is spent — watch a rewarded ad to unlock it,
                        // same flow as running out of hints entirely below.
                        hintsRemaining > 0 && activity != null -> {
                            watchingHintAd = true
                            AdsManager.showRewarded(
                                activity = activity,
                                onReward = { useHint() },
                                onClosed = { watchingHintAd = false },
                                onNotReady = { watchingHintAd = false }
                            )
                        }
                        // Out of hints entirely — offer a rewarded ad for one
                        // more instead of just leaving the badge dead once
                        // the level's whole allowance runs out.
                        activity != null -> {
                            watchingHintAd = true
                            AdsManager.showRewarded(
                                activity = activity,
                                onReward = {
                                    hintsRemaining++
                                    useHint()
                                },
                                onClosed = { watchingHintAd = false },
                                onNotReady = { watchingHintAd = false }
                            )
                        }
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(icon = Icons.AutoMirrored.Filled.DirectionsWalk, value = "$moveCount")
                StatChip(icon = Icons.Filled.Timer, value = formatTime(elapsedSeconds))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .aspectRatio(level.cols.toFloat() / level.rows.toFloat())
                    .clip(RoundedCornerShape(14.dp))
            ) {
                MazeBoard(
                    level = level,
                    grid = grid,
                    path = path,
                    hintPath = hintPath,
                    hintTravel = hintTravel,
                    hintFade = hintFade,
                    animRow = animRow,
                    animCol = animCol,
                    shake = shake,
                    celebration = celebration,
                    distanceFromStart = distanceFromStart,
                    maxDistance = maxDistance,
                    gridReveal = gridReveal,
                    playerEntrance = playerEntrance,
                    enemyAnimRows = enemyAnimRows,
                    enemyAnimCols = enemyAnimCols,
                    enemyDirections = enemyDirections,
                    playerPhoto = playerPhoto,
                    enemyPhotos = enemyPhotos,
                    playerColor = playerSkinColor,
                    enemyColor = enemySkinColor,
                    accentColor = categoryAccent,
                    boardActive = !isPaused && !isWon && !isCaught && !showTutorial,
                    onAttemptMove = { dir -> tryMove(dir) }
                )

                // Tap-based fallback controls — swipes can occasionally be
                // misread (too short, wrong axis, quick double-flick), which
                // is what makes the player feel "stuck" in place. These
                // buttons call tryMove() directly, so a move always lands.
                if (onScreenControlsEnabled) {
                    DPad(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        onMove = { dir -> tryMove(dir) }
                    )
                }
            }
        }

        if (isPaused && !isWon) {
            PauseOverlay(
                onResume = {
                    isPaused = false
                    if (hasEnemyThreat) SoundManager.resumeBackgroundMusic()
                },
                onRestart = { showRetryConfirm = true },
                onHome = { goHome() },
                vibrationEnabled = vibrationEnabled,
                onToggleVibration = {
                    vibrationEnabled = !vibrationEnabled
                    SettingsPrefs.setVibrationEnabled(context, vibrationEnabled)
                },
                onScreenControlsEnabled = onScreenControlsEnabled,
                onToggleOnScreenControls = {
                    onScreenControlsEnabled = !onScreenControlsEnabled
                    SettingsPrefs.setOnScreenControlsEnabled(context, onScreenControlsEnabled)
                }
            )
        }

        if (showWinOverlay) {
            WinOverlay(
                perfectRun = perfectRun,
                moveCount = moveCount,
                optimalMoves = optimalMoves,
                elapsedSeconds = elapsedSeconds,
                hasNextLevel = hasNextLevel,
                hasEnemies = level.enemies.isNotEmpty(),
                onHome = { goHome() },
                onReplay = { resetLevel() },
                onNext = { goNextLevel() },
                onNextLocked = { showLockedDialog = true }
            )
        }

        if (showLockedDialog) {
            LockedLevelNotice(onDismiss = { showLockedDialog = false })
        }

        if (showRetryConfirm) {
            RetryConfirmDialog(
                onCancel = { showRetryConfirm = false },
                onConfirm = {
                    showRetryConfirm = false
                    resetLevel()
                }
            )
        }

        if (isCaught) {
            CaughtOverlay(
                playerPhoto = playerPhoto,
                onRetry = { resetLevel() },
                onHome = { goHome() }
            )
        }

        if (showTutorial) {
            GameTutorialOverlay(
                hintsAvailable = level.maxHints > 0,
                onFinish = {
                    showTutorial = false
                    OnboardingPrefs.setGameTutorialSeen(context)
                }
            )
        }
    }
}

@Composable
private fun DPad(modifier: Modifier = Modifier, onMove: (Direction) -> Unit) {
    val btnSize = 46.dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DPadButton(icon = Icons.Filled.KeyboardArrowUp, size = btnSize, onClick = { onMove(Direction.NORTH) })
        Row(verticalAlignment = Alignment.CenterVertically) {
            DPadButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft, size = btnSize, onClick = { onMove(Direction.WEST) })
            Spacer(Modifier.size(btnSize))
            DPadButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowRight, size = btnSize, onClick = { onMove(Direction.EAST) })
        }
        DPadButton(icon = Icons.Filled.KeyboardArrowDown, size = btnSize, onClick = { onMove(Direction.SOUTH) })
    }
}

@Composable
private fun DPadButton(icon: androidx.compose.ui.graphics.vector.ImageVector, size: Dp, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.34f))
            .bounceClick(interactionSource, playSound = false)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(size * 0.6f))
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    // A quick one-shot pop-in when the chip first appears (fresh level load)
    // instead of snapping straight to full size — purely cosmetic, costs
    // nothing once settled since the Animatable stops animating at 1f.
    val entrance = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 380f))
    }
    Row(
        modifier = Modifier
            .graphicsLayer { scaleX = entrance.value; scaleY = entrance.value; alpha = entrance.value.coerceIn(0f, 1f) }
            .clip(RoundedCornerShape(12.dp))
            .background(CardLocked)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
        Text(
            text = " $value",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GameTopBar(
    title: String,
    hintsRemaining: Int,
    hintsEnabled: Boolean,
    onPause: () -> Unit,
    onRestart: () -> Unit,
    onHint: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RoundIconButton(background = AccentGold, icon = Icons.Filled.Pause, contentDescription = "Pause", playSound = false, onClick = onPause)
            RoundIconButton(background = CardLocked, icon = Icons.Filled.Replay, contentDescription = "Restart", contentColor = TextPrimary, playSound = false, onClick = onRestart)
            HintBadge(count = hintsRemaining, enabled = hintsEnabled, onClick = onHint)
        }
    }
}

@Composable
private fun RoundIconButton(
    background: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String? = null,
    contentColor: Color = Color.White,
    size: Dp = 44.dp,
    playSound: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .bounceClick(interactionSource, playSound = playSound)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
private fun HintBadge(count: Int, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    // A slow breathing glow behind the badge invites a tap when hints are
    // available, instead of sitting completely static.
    val infinite = rememberInfiniteTransition(label = "hint_badge_glow")
    val glow by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(950, easing = LinearEasing), RepeatMode.Reverse),
        label = "hint_glow"
    )

    Box(contentAlignment = Alignment.Center) {
        if (enabled && count > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(scaleX = glow, scaleY = glow)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentAmber.copy(alpha = 0.10f))
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (enabled) AccentAmber.copy(alpha = 0.16f) else CardLocked)
                .bounceClick(interactionSource, playSound = false)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = "Hint",
                tint = if (enabled) AccentAmber else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = " $count",
                color = if (enabled) AccentAmber else TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MazeBoard(
    level: MazeLevel,
    grid: MazeGrid,
    path: List<CellPos>,
    hintPath: List<CellPos>,
    hintTravel: Animatable<Float, AnimationVector1D>,
    hintFade: Animatable<Float, AnimationVector1D>,
    animRow: Animatable<Float, AnimationVector1D>,
    animCol: Animatable<Float, AnimationVector1D>,
    shake: Animatable<Float, AnimationVector1D>,
    celebration: Animatable<Float, AnimationVector1D>,
    distanceFromStart: Array<IntArray>,
    maxDistance: Int,
    gridReveal: Animatable<Float, AnimationVector1D>,
    playerEntrance: Animatable<Float, AnimationVector1D>,
    enemyAnimRows: List<Animatable<Float, AnimationVector1D>> = emptyList(),
    enemyAnimCols: List<Animatable<Float, AnimationVector1D>> = emptyList(),
    enemyDirections: List<Direction> = emptyList(),
    playerPhoto: ImageBitmap? = null,
    enemyPhotos: List<ImageBitmap> = emptyList(),
    playerColor: Color = AccentGold,
    enemyColor: Color = EnemyColor,
    accentColor: Color = AccentGold,
    boardActive: Boolean = true,
    onAttemptMove: (Direction) -> Boolean
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // A slow, continuous pulse used for the goal ring and the hint dot.
    //
    // These used to run on a single always-on rememberInfiniteTransition,
    // which meant the Canvas kept redrawing the *entire* maze grid at a
    // steady 60fps even while completely hidden behind the Pause/Win/Caught
    // overlays — pure wasted work that got worse on bigger, later-level
    // mazes (more cells = more draw calls per wasted frame), which is
    // exactly the "lag builds up towards the end levels" symptom this was
    // causing. Each pulse now runs its own small manual loop keyed off
    // [boardActive], so the moment an overlay covers the board the
    // animation coroutine is cancelled — value stops changing, Canvas stops
    // redrawing — and resumes cleanly the instant play continues.
    val goalPulseAnim = remember(level) { Animatable(0.92f) }
    LaunchedEffect(level, boardActive) {
        if (!boardActive) return@LaunchedEffect
        while (true) {
            goalPulseAnim.animateTo(1.1f, tween(1100, easing = LinearEasing))
            goalPulseAnim.animateTo(0.92f, tween(1100, easing = LinearEasing))
        }
    }
    val goalPulse = goalPulseAnim.value

    // Gentle up/down float so idle guards don't look frozen in place.
    val ghostBobAnim = remember(level) { Animatable(-1f) }
    LaunchedEffect(level, boardActive) {
        if (!boardActive) return@LaunchedEffect
        while (true) {
            ghostBobAnim.animateTo(1f, tween(620, easing = LinearEasing))
            ghostBobAnim.animateTo(-1f, tween(620, easing = LinearEasing))
        }
    }
    val ghostBob = ghostBobAnim.value

    val hintPulseAnim = remember(level) { Animatable(0.65f) }
    LaunchedEffect(level, boardActive) {
        if (!boardActive) return@LaunchedEffect
        while (true) {
            hintPulseAnim.animateTo(1f, tween(420, easing = LinearEasing))
            hintPulseAnim.animateTo(0.65f, tween(420, easing = LinearEasing))
        }
    }
    val hintPulse = hintPulseAnim.value

    val boardBackgroundBrush =
        Brush.verticalGradient(listOf(BackgroundTop.copy(alpha = 0.4f), BackgroundTop.copy(alpha = 0.4f)))

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(boardBackgroundBrush)
            .onSizeChanged { canvasSize = it }
            .mazeDragGestures(level, canvasSize, onAttemptMove)
    ) {
        val cellSize = size.width / level.cols
        val wallWidth = cellSize * 0.055f

        // Walls — each cell fades/slides in as the reveal wave passes over it, so the
        // corridors look like they're being carved out from the entrance outward.
        val wavefront = gridReveal.value * (maxDistance + 2)
        val waveBand = 1.6f // how many "distance steps" wide the soft fade edge is
        for (r in 0 until level.rows) {
            for (c in 0 until level.cols) {
                val cell = grid.cellAt(r, c)
                val rawDist = distanceFromStart[r][c]
                val cellDist = if (rawDist >= 0) rawDist else (r + c)
                val cellReveal = ((wavefront - cellDist) / waveBand).coerceIn(0f, 1f)
                if (cellReveal <= 0f) continue

                val x0 = c * cellSize
                val y0 = r * cellSize
                val x1 = x0 + cellSize
                val y1 = y0 + cellSize
                val isStartCell = r == level.start.row && c == level.start.col
                // Cells pop in with a tiny inward "settle" — walls start a touch shorter
                // and snap to full length, echoing the ease-out used for the wave itself.
                val settle = 0.5f + 0.5f * cellReveal
                val cx = (x0 + x1) / 2f
                val cy = (y0 + y1) / 2f
                val wallColor = TextPrimary.copy(alpha = cellReveal)

                if (cell.open[Direction.NORTH] != true) {
                    drawLine(
                        wallColor,
                        settledOffset(x0, y0, cx, cy, settle),
                        settledOffset(x1, y0, cx, cy, settle),
                        wallWidth, StrokeCap.Round
                    )
                }
                if (cell.open[Direction.WEST] != true && !isStartCell) {
                    drawLine(
                        wallColor,
                        settledOffset(x0, y0, cx, cy, settle),
                        settledOffset(x0, y1, cx, cy, settle),
                        wallWidth, StrokeCap.Round
                    )
                }
                if (cell.open[Direction.EAST] != true) {
                    drawLine(
                        wallColor,
                        settledOffset(x1, y0, cx, cy, settle),
                        settledOffset(x1, y1, cx, cy, settle),
                        wallWidth, StrokeCap.Round
                    )
                }
                if (cell.open[Direction.SOUTH] != true) {
                    drawLine(
                        wallColor,
                        settledOffset(x0, y1, cx, cy, settle),
                        settledOffset(x1, y1, cx, cy, settle),
                        wallWidth, StrokeCap.Round
                    )
                }
            }
        }

        // Entrance stub outside the start cell — appears with the very first wave tick
        val entranceReveal = (wavefront / waveBand).coerceIn(0f, 1f)
        val startCenterY = level.start.row * cellSize + cellSize / 2f
        drawLine(
            color = accentColor.copy(alpha = entranceReveal),
            start = Offset(-cellSize * 0.32f, startCenterY),
            end = Offset(0f, startCenterY),
            strokeWidth = cellSize * 0.12f,
            cap = StrokeCap.Round
        )

        // Goal marker — soft outer glow + pulsing ring, fades in once the wave reaches it.
        val goalDist = distanceFromStart[level.goal.row][level.goal.col]
            .takeIf { it >= 0 } ?: maxDistance
        val goalReveal = ((wavefront - goalDist) / waveBand).coerceIn(0f, 1f)
        val goalCenter = Offset(
            level.goal.col * cellSize + cellSize / 2f,
            level.goal.row * cellSize + cellSize / 2f
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.18f * goalReveal),
            radius = cellSize * 0.46f * goalPulse,
            center = goalCenter
        )
        drawCircle(
            color = accentColor.copy(alpha = goalReveal),
            radius = cellSize * 0.32f * goalPulse,
            center = goalCenter,
            style = Stroke(width = cellSize * 0.075f)
        )

        // Goal-reached celebration particles
        val progress = celebration.value
        if (progress > 0f && progress < 1f) {
            val particleCount = 14
            for (i in 0 until particleCount) {
                val angle = (i / particleCount.toFloat()) * (2f * Math.PI.toFloat()) + i
                val dist = cellSize * 1.0f * progress
                val px = goalCenter.x + cos(angle) * dist
                val py = goalCenter.y + sin(angle) * dist
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val radius = cellSize * 0.07f * (1f - progress * 0.5f)
                val color = if (i % 2 == 0) accentColor else AccentAmber
                drawCircle(color = color.copy(alpha = alpha), radius = radius, center = Offset(px, py))
            }
        }

        // Enemy patrol guards — classic ghost silhouette with directional eyes.
        // The heartbeat loop (already playing for the life of any level with
        // guards) gives an early audio warning before one comes into view.
        if (playerEntrance.value > 0f) {
            val baseAlpha = playerEntrance.value.coerceIn(0f, 1f)
            val bobPx = cellSize * 0.05f * ghostBob
            for (i in enemyAnimRows.indices) {
                val er = enemyAnimRows[i].value
                val ec = enemyAnimCols[i].value
                val enemyAlpha = baseAlpha
                if (enemyAlpha <= 0.02f) continue
                val ex = ec * cellSize + cellSize / 2f
                val ey = er * cellSize + cellSize / 2f + bobPx
                val lookDir = enemyDirections.getOrNull(i) ?: Direction.SOUTH
                val enemyPhoto = enemyPhotos.takeIf { it.isNotEmpty() }?.let { it[i % it.size] }
                drawEnemyGhost(center = Offset(ex, ey), cellSize = cellSize, alpha = enemyAlpha, lookDir = lookDir, bodyColor = enemyColor, photo = enemyPhoto)
            }
        }

        // Player marker — no trail is drawn behind it; the path is only used
        // to know where the player currently is.
        if (path.isNotEmpty() && playerEntrance.value > 0f) {
            val trailAlpha = playerEntrance.value.coerceIn(0f, 1f)
            val animatedX = animCol.value * cellSize + cellSize / 2f
            val animatedY = animRow.value * cellSize + cellSize / 2f
            val shakeOffsetPx = shake.value * cellSize * 0.12f
            val playerCenter = Offset(animatedX + shakeOffsetPx, animatedY)

            // Player pops in with a springy overshoot (>1f briefly) rather than snapping to size.
            val playerScale = playerEntrance.value
            drawPlayerHero(center = playerCenter, cellSize = cellSize, alpha = trailAlpha, scale = playerScale, bob = ghostBob, bodyColor = playerColor, photo = playerPhoto)

            if (hintPath.size >= 2) {
                val hintPoints = hintPath.map { cell ->
                    Offset(cell.col * cellSize + cellSize / 2f, cell.row * cellSize + cellSize / 2f)
                }
                drawHintTrail(
                    points = hintPoints,
                    travel = hintTravel.value,
                    fade = hintFade.value,
                    pulse = hintPulse,
                    dotRadius = cellSize * 0.12f
                )
            }
        }
    }
}

/**
 * Draws the player as a small "spark spirit" explorer — a rounded teardrop
 * body with two little points on top (like a soft flame/ear silhouette), a
 * bright core glow, and a friendly blinking-eyed face. Replaces the old
 * plain colored dot with something that actually reads as a character.
 */
/**
 * Draws a pre-masked circular [photo] (see [CharacterPhoto] — the alpha
 * mask is baked in once at load time, not clipped per frame) at [center]
 * with the given [radius], plus a thin ring in [ringColor] so it still
 * reads as "the player" / "a guard" against the corridors, the same way
 * the illustrated art used a color-coded glow.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCircularPhoto(
    photo: ImageBitmap,
    center: Offset,
    radius: Float,
    alpha: Float,
    ringColor: Color
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val diameter = (radius * 2f)
    drawImage(
        image = photo,
        dstOffset = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
        dstSize = IntSize(diameter.roundToInt(), diameter.roundToInt()),
        alpha = alpha
    )
    drawCircle(
        color = ringColor.copy(alpha = 0.9f * alpha),
        radius = radius,
        center = center,
        style = Stroke(width = radius * 0.12f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlayerHero(
    center: Offset,
    cellSize: Float,
    alpha: Float,
    scale: Float,
    bob: Float,
    bodyColor: Color = AccentGold,
    photo: ImageBitmap? = null
) {
    val r = cellSize * 0.30f * scale
    val bodyCenter = center + Offset(0f, -r * 0.06f * bob)

    // Outer aura so the hero pops against the corridors, same language as the guard's glow.
    drawCircle(
        color = bodyColor.copy(alpha = 0.24f * alpha),
        radius = r * 1.7f,
        center = bodyCenter
    )

    if (photo != null) {
        drawCircularPhoto(photo = photo, center = bodyCenter, radius = r, alpha = alpha, ringColor = bodyColor)
        return
    }

    // Teardrop body: rounded bottom, two soft points at the top like little ears/flame tips.
    val body = Path().apply {
        moveTo(bodyCenter.x, bodyCenter.y - r * 1.35f)
        quadraticBezierTo(
            bodyCenter.x - r * 0.55f, bodyCenter.y - r * 1.05f,
            bodyCenter.x - r * 0.62f, bodyCenter.y - r * 0.35f
        )
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                bodyCenter.x - r, bodyCenter.y - r * 0.35f,
                bodyCenter.x + r, bodyCenter.y + r * 1.65f
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        quadraticBezierTo(
            bodyCenter.x + r * 0.62f, bodyCenter.y - r * 1.05f,
            bodyCenter.x, bodyCenter.y - r * 1.35f
        )
        close()
    }

    drawPath(path = body, color = bodyColor.copy(alpha = 0.95f * alpha))
    // Bright core so it reads like a glowing little spirit rather than a flat shape.
    drawCircle(
        color = Color.White.copy(alpha = 0.30f * alpha),
        radius = r * 0.4f,
        center = bodyCenter + Offset(-r * 0.15f, r * 0.15f)
    )

    // Friendly face: two round eyes with shine, small closed-smile curve.
    val eyeY = bodyCenter.y + r * 0.05f
    val eyeSpacing = r * 0.36f
    val eyeRadius = r * 0.20f
    for (side in listOf(-1f, 1f)) {
        val eyeCenter = Offset(bodyCenter.x + side * eyeSpacing, eyeY)
        drawCircle(color = Color.White.copy(alpha = alpha), radius = eyeRadius, center = eyeCenter)
        drawCircle(
            color = Color(0xFF10361F).copy(alpha = alpha),
            radius = eyeRadius * 0.5f,
            center = eyeCenter + Offset(0f, eyeRadius * 0.15f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f * alpha),
            radius = eyeRadius * 0.18f,
            center = eyeCenter + Offset(-eyeRadius * 0.25f, -eyeRadius * 0.25f)
        )
    }
    val smile = Path().apply {
        moveTo(bodyCenter.x - r * 0.22f, bodyCenter.y + r * 0.42f)
        quadraticBezierTo(
            bodyCenter.x, bodyCenter.y + r * 0.62f,
            bodyCenter.x + r * 0.22f, bodyCenter.y + r * 0.42f
        )
    }
    drawPath(
        path = smile,
        color = Color(0xFF10361F).copy(alpha = 0.8f * alpha),
        style = Stroke(width = r * 0.09f, cap = StrokeCap.Round)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEnemyGhost(
    center: Offset,
    cellSize: Float,
    alpha: Float,
    lookDir: Direction,
    bodyColor: Color = EnemyColor,
    photo: ImageBitmap? = null
) {
    val r = cellSize * 0.30f
    val bodyTop = center.y - r * 1.15f
    val bodyBottom = center.y + r * 0.78f

    // Soft outer glow, a shade wider and hotter than the hero's aura so the
    // threat reads immediately even before the player spots the shape.
    drawCircle(
        color = bodyColor.copy(alpha = 0.26f * alpha),
        radius = r * 1.85f,
        center = Offset(center.x, center.y - r * 0.1f)
    )

    if (photo != null) {
        drawCircularPhoto(
            photo = photo,
            center = Offset(center.x, center.y - r * 0.1f),
            radius = r,
            alpha = alpha,
            ringColor = bodyColor
        )
        return
    }

    val body = Path().apply {
        moveTo(center.x - r, bodyBottom)
        // Three jagged spikes replace the smooth rounded head of a plain
        // ghost, giving the guard a sharper, more hostile silhouette.
        val spikeWidth = (r * 2f) / 3f
        val spikeHeight = r * 0.85f
        var x = center.x - r
        lineTo(x, bodyTop + r * 0.35f)
        repeat(3) {
            val xMid = x + spikeWidth / 2f
            val xNext = x + spikeWidth
            lineTo(xMid, bodyTop - spikeHeight * (if (it == 1) 1f else 0.7f))
            lineTo(xNext, bodyTop + r * 0.35f)
            x = xNext
        }
        lineTo(center.x + r, bodyBottom)
        // Three rounded scallops form the wavy "hem" feet, like a classic ghost sprite.
        val legWidth = (r * 2f) / 3f
        val footDepth = r * 0.38f
        var fx = center.x + r
        repeat(3) {
            val xMid = fx - legWidth / 2f
            val xNext = fx - legWidth
            quadraticBezierTo(xMid, bodyBottom + footDepth, xNext, bodyBottom)
            fx = xNext
        }
        close()
    }

    drawPath(path = body, color = bodyColor.copy(alpha = 0.92f * alpha))
    // Subtle top-left highlight for a touch of volume.
    drawCircle(
        color = Color.White.copy(alpha = 0.14f * alpha),
        radius = r * 0.5f,
        center = Offset(center.x - r * 0.3f, bodyTop + r * 0.5f)
    )

    // Angled brow shadows above each eye give the face a scowling, alert look.
    val eyeY = center.y - r * 0.16f
    val eyeSpacing = r * 0.42f
    val eyeRadius = r * 0.30f
    val pupilRadius = r * 0.15f
    for (side in listOf(-1f, 1f)) {
        val browCenter = Offset(center.x + side * eyeSpacing, eyeY - eyeRadius * 1.15f)
        val brow = Path().apply {
            moveTo(browCenter.x - eyeRadius * 0.9f, browCenter.y + eyeRadius * (if (side < 0) 0.35f else -0.35f))
            lineTo(browCenter.x + eyeRadius * 0.9f, browCenter.y + eyeRadius * (if (side < 0) -0.35f else 0.35f))
        }
        drawPath(
            path = brow,
            color = Color(0xFF2B1B3D).copy(alpha = 0.85f * alpha),
            style = Stroke(width = eyeRadius * 0.28f, cap = StrokeCap.Round)
        )
    }

    // Eyes glow a hot ember color instead of a plain dark pupil, and shift
    // toward the direction the guard is walking.
    val pupilShiftX = lookDir.colDelta * eyeRadius * 0.45f
    val pupilShiftY = lookDir.rowDelta * eyeRadius * 0.45f
    for (side in listOf(-1f, 1f)) {
        val eyeCenter = Offset(center.x + side * eyeSpacing, eyeY)
        drawCircle(color = Color.White.copy(alpha = alpha), radius = eyeRadius, center = eyeCenter)
        drawCircle(
            color = Color(0xFFFFC24B).copy(alpha = alpha),
            radius = pupilRadius,
            center = eyeCenter + Offset(pupilShiftX, pupilShiftY)
        )
        drawCircle(
            color = Color(0xFF2B1B3D).copy(alpha = alpha),
            radius = pupilRadius * 0.4f,
            center = eyeCenter + Offset(pupilShiftX, pupilShiftY)
        )
    }
}

/**
 * Draws the "Guiding Light" hint trail: a soft glowing line connecting every
 * revealed cell ahead of the player, fading waypoint dots along it, and a
 * bright little spark that visibly travels the whole stretch once when the
 * hint is used. This replaces the old single-arrow flash, which only ever
 * showed the *very next* step and vanished before most players could react
 * to it — especially useless right at a junction, where seeing 2-3 steps
 * ahead is exactly what's needed to commit to the correct branch.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHintTrail(
    points: List<Offset>,
    travel: Float,
    fade: Float,
    pulse: Float,
    dotRadius: Float
) {
    if (points.size < 2 || fade <= 0f) return

    // Soft connecting line through the whole revealed stretch, so the route
    // reads as one continuous path rather than disconnected dots.
    val linePath = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
    }
    drawPath(
        path = linePath,
        color = AccentAmber.copy(alpha = 0.30f * fade),
        style = Stroke(width = dotRadius * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // A dot at each waypoint (skipping the player's own cell), gently fading
    // toward the far end so the eye is pulled back toward the player rather
    // than lost staring at the last cell.
    for (i in 1 until points.size) {
        val fraction = i / (points.size - 1f)
        val dotAlpha = (0.85f - fraction * 0.45f) * fade
        drawCircle(
            color = AccentAmber.copy(alpha = dotAlpha),
            radius = dotRadius * (0.55f + 0.15f * pulse),
            center = points[i]
        )
    }

    // The traveling spark: runs the full length of the revealed stretch once
    // when the hint fires, like a little guiding light walking the route
    // ahead of you, then keeps glowing at the far end while the trail fades.
    val totalSegments = points.size - 1
    val scaled = (travel * totalSegments).coerceIn(0f, totalSegments.toFloat())
    val segIndex = scaled.toInt().coerceIn(0, totalSegments - 1)
    val segFraction = scaled - segIndex
    val from = points[segIndex]
    val to = points[segIndex + 1]
    val sparkCenter = Offset(
        from.x + (to.x - from.x) * segFraction,
        from.y + (to.y - from.y) * segFraction
    )
    drawCircle(color = AccentAmber.copy(alpha = 0.35f * fade), radius = dotRadius * 2.1f, center = sparkCenter)
    drawCircle(color = Color.White.copy(alpha = 0.9f * fade), radius = dotRadius * 0.85f, center = sparkCenter)
}

private fun Modifier.mazeDragGestures(
    level: MazeLevel,
    canvasSize: IntSize,
    onAttemptMove: (Direction) -> Boolean
): Modifier = pointerInput(level, canvasSize) {
    var accumulator = Offset.Zero
    // Once a gesture commits to horizontal or vertical, it stays locked to that axis
    // for the rest of the swipe — this is what stops a slightly-diagonal thumb swipe
    // from firing a wrong turn partway through.
    var lockedAxisIsHorizontal: Boolean? = null
    var lastEventTimeMillis = 0L

    detectDragGestures(
        onDragStart = {
            accumulator = Offset.Zero
            lockedAxisIsHorizontal = null
            lastEventTimeMillis = 0L
        },
        onDragEnd = { accumulator = Offset.Zero; lockedAxisIsHorizontal = null },
        onDragCancel = { accumulator = Offset.Zero; lockedAxisIsHorizontal = null }
    ) { change, dragAmount ->
        change.consume()
        val cellPx = if (canvasSize.width > 0) canvasSize.width / level.cols.toFloat() else 0f
        if (cellPx <= 0f) return@detectDragGestures

        // A fast flick (large movement in a single pointer event) should feel more
        // responsive than a slow, deliberate drag — so it gets a lower threshold.
        val eventMillis = change.uptimeMillis
        val dtMillis = (eventMillis - lastEventTimeMillis).coerceAtLeast(1L)
        lastEventTimeMillis = eventMillis
        val speed = kotlin.math.hypot(dragAmount.x, dragAmount.y) / dtMillis // px/ms
        val isFlick = speed > cellPx * 0.012f
        val baseThreshold = if (isFlick) cellPx * 0.30f else cellPx * 0.42f

        accumulator += dragAmount

        // Decide (and lock) the gesture's axis the first time it clears a small deadzone.
        if (lockedAxisIsHorizontal == null) {
            val deadzone = cellPx * 0.16f
            if (maxOf(abs(accumulator.x), abs(accumulator.y)) >= deadzone) {
                lockedAxisIsHorizontal = abs(accumulator.x) > abs(accumulator.y)
            }
        }

        var safety = 0
        while (safety < 14) {
            safety++
            val horizontal = lockedAxisIsHorizontal ?: (abs(accumulator.x) > abs(accumulator.y))
            val magnitude = if (horizontal) abs(accumulator.x) else abs(accumulator.y)
            if (magnitude < baseThreshold) break

            val dir = if (horizontal) {
                if (accumulator.x > 0) Direction.EAST else Direction.WEST
            } else {
                if (accumulator.y > 0) Direction.SOUTH else Direction.NORTH
            }

            val moved = onAttemptMove(dir)
            accumulator = if (horizontal) {
                accumulator.copy(x = accumulator.x - baseThreshold * sign(accumulator.x))
            } else {
                accumulator.copy(y = accumulator.y - baseThreshold * sign(accumulator.y))
            }
            if (!moved) {
                accumulator = Offset.Zero
                break
            }
        }
    }
}

private data class TutorialStep(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val body: String
)

/**
 * A short, skippable walkthrough shown once, the very first time a new
 * player opens any level — before this, the app dropped people straight
 * into the maze with zero explanation of swipe controls, hints, pause, or
 * how stars are earned. Blocks gameplay input underneath (see the
 * `showTutorial` guards in MazeGameScreen) while it's up, then never shows
 * again automatically once dismissed.
 */
@Composable
private fun GameTutorialOverlay(hintsAvailable: Boolean, onFinish: () -> Unit) {
    val steps = remember(hintsAvailable) {
        buildList {
            add(
                TutorialStep(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    title = "Swipe to move",
                    body = "Drag anywhere on the maze — up, down, left or right — to guide your spark through the corridors. No swipe needed? The little arrow pad in the corner taps you through instead."
                )
            )
            if (hintsAvailable) {
                add(
                    TutorialStep(
                        icon = Icons.Filled.Lightbulb,
                        title = "Stuck? Use a hint",
                        body = "Tap the lightbulb to light up several real steps of the route ahead — a little spark travels the path for you, so it actually helps at junctions, not just one step."
                    )
                )
            }
            add(
                TutorialStep(
                    icon = Icons.Filled.Pause,
                    title = "Pause or restart anytime",
                    body = "The pause icon lets you take a break without losing progress. The replay icon resets the level fresh if you'd rather start over."
                )
            )
            add(
                TutorialStep(
                    icon = Icons.Filled.EmojiEvents,
                    title = "Reach the exit",
                    body = "Get from the entrance to the exit to clear the level and unlock the next one. That's it — no scoring, just get through."
                )
            )
        }
    }

    var stepIndex by remember { mutableStateOf(0) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            // Swallow every touch so nothing underneath (maze drag, D-pad,
            // top-bar buttons) can be triggered while the guide is up.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(220)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(dampingRatio = 0.62f, stiffness = 300f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(BackgroundBottom)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(14.dp))

                // Crossfade between steps rather than a hard cut, keyed on the
                // step index so each new step pops in with its own small entrance.
                androidx.compose.animation.Crossfade(targetState = stepIndex, label = "tutorial_step") { idx ->
                    val step = steps[idx]
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AccentGold.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = step.icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = step.title,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = step.body,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Progress dots — the current step glows brighter and a touch
                // wider so it's obvious how far through the guide you are.
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .size(width = if (i == stepIndex) 18.dp else 6.dp, height = 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i == stepIndex) AccentGold else CardLocked)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                val isLastStep = stepIndex == steps.lastIndex
                OverlayButton(
                    text = if (isLastStep) "LET'S GO" else "NEXT",
                    background = AccentGold,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isLastStep) onFinish() else stepIndex++
                    }
                )
                if (!isLastStep) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "SKIP",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onFinish
                            )
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    vibrationEnabled: Boolean,
    onToggleVibration: () -> Unit,
    onScreenControlsEnabled: Boolean,
    onToggleOnScreenControls: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            // Consume every touch on the scrim so a tap/swipe can never pass
            // through to the maze board or D-pad sitting underneath while
            // the game is paused — without this, a swipe starting on the
            // dimmed background could still reach the drag-gesture area below.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(BackgroundBottom)
                .border(width = 2.dp, color = TextSecondary)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PAUSED", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(20.dp))
            OverlayButton(text = "RESUME", background = AccentGold, onClick = onResume)
            Spacer(Modifier.height(10.dp))
            OverlayButton(text = "RESTART", background = CardLocked, textColor = TextPrimary, onClick = onRestart)
            Spacer(Modifier.height(10.dp))
            OverlayButton(text = "HOME", background = CardLocked, textColor = TextPrimary, onClick = onHome)

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PauseToggleButton(
                    label = "VIBRATION",
                    enabled = vibrationEnabled,
                    onClick = onToggleVibration,
                    modifier = Modifier.weight(1f)
                )
                PauseToggleButton(
                    label = "D-PAD",
                    enabled = onScreenControlsEnabled,
                    onClick = onToggleOnScreenControls,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PauseToggleButton(label: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(40.dp)
            .background(if (enabled) AccentGold else CardLocked)
            .border(width = 1.dp, color = TextSecondary)
            .bounceClick(interactionSource, playSound = false)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label ${if (enabled) "ON" else "OFF"}",
            color = if (enabled) BackgroundTop else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WinOverlay(
    perfectRun: Boolean,
    moveCount: Int,
    optimalMoves: Int,
    elapsedSeconds: Int,
    hasNextLevel: Boolean,
    hasEnemies: Boolean = false,
    onHome: () -> Unit,
    onReplay: () -> Unit,
    onNext: () -> Unit,
    onNextLocked: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val titleColor = if (hasEnemies) EnemyColor else AccentGold

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            // Same pass-through fix as the pause scrim — the win card sits on
            // top visually, but without this a swipe could still reach the
            // (now-inactive) board underneath and feel like the game glitched.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        FloatingSparkles(modifier = Modifier.fillMaxSize(), accent = if (hasEnemies) EnemyColor else AccentAmber)

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(260)) + scaleIn(
                initialScale = 0.75f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 260f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(listOf(BackgroundBottom, BackgroundTop)))
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (hasEnemies) "GUARDS EVADED!" else "LEVEL COMPLETE!",
                    color = titleColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        perfectRun && hasEnemies -> "Perfect run — not a single guard laid eyes on you. Ghost mode."
                        perfectRun -> "Perfect run — no wasted steps. Show-off."
                        hasEnemies -> "You made it past every patrol — nice moves!"
                        else -> "Great maze-running!"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                if (perfectRun) {
                    Spacer(Modifier.height(8.dp))
                    PerfectBadge()
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatPill(label = "MOVES", value = "$moveCount")
                    StatPill(label = "BEST", value = "$optimalMoves")
                    StatPill(label = "TIME", value = formatTime(elapsedSeconds))
                }

                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    RoundIconButton(background = CardLocked, icon = Icons.Filled.Home, contentDescription = "Home", contentColor = TextPrimary, onClick = onHome)
                    RoundIconButton(background = AccentGold, icon = Icons.Filled.Replay, contentDescription = "Replay", size = 54.dp, onClick = onReplay)
                    if (hasNextLevel) {
                        RoundIconButton(background = AccentAmber, icon = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next level", onClick = onNext)
                    } else {
                        RoundIconButton(background = CardLocked, icon = Icons.Filled.Lock, contentDescription = "Locked", contentColor = TextSecondary, onClick = onNextLocked)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    if (hasNextLevel) "Next maze is ready — good luck!" else "More levels in this category are on the way!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PerfectBadge() {
    val scale = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "perfect_shimmer")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "perfect_shimmer_alpha"
    )
    LaunchedEffect(Unit) {
        delay(520)
        scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 350f))
    }
    Row(
        modifier = Modifier
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .clip(RoundedCornerShape(20.dp))
            .background(AccentAmber.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(12.dp).graphicsLayer(alpha = shimmer))
        Text(
            " PERFECT",
            color = AccentAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

private data class SparkleSpec(
    val xFrac: Float,
    val yFrac: Float,
    val phase: Float,
    val sizeSp: Int,
    val symbol: String
)

@Composable
private fun FloatingSparkles(modifier: Modifier = Modifier, accent: Color = AccentAmber) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle_time")
    val t by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "sparkle_progress"
    )
    val sparkles = remember {
        List(10) { i ->
            SparkleSpec(
                xFrac = Random(i * 91 + 7).nextFloat(),
                yFrac = Random(i * 37 + 3).nextFloat() * 0.85f,
                phase = Random(i * 13 + 1).nextFloat(),
                sizeSp = 10 + Random(i * 5 + 2).nextInt(10),
                symbol = if (i % 2 == 0) "✦" else "✧"
            )
        }
    }
    BoxWithConstraints(modifier = modifier) {
        val w = maxWidth
        val h = maxHeight
        sparkles.forEach { s ->
            val cycle = (t + s.phase) % 1f
            val alpha = ((sin(cycle * 2f * Math.PI.toFloat()) * 0.5f + 0.5f) * 0.5f).coerceIn(0f, 1f)
            val riseDp = (-cycle * 26f).dp
            Text(
                text = s.symbol,
                color = accent.copy(alpha = alpha),
                fontSize = s.sizeSp.sp,
                modifier = Modifier.offset(x = w * s.xFrac, y = h * s.yFrac + riseDp)
            )
        }
    }
}

@Composable
private fun RetryConfirmDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val dismissInteraction = remember { MutableInteractionSource() }
    val swallowInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(interactionSource = dismissInteraction, indication = null, onClick = onCancel),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 320f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(AccentGold, AccentGold.copy(alpha = 0.78f))))
                    .clickable(interactionSource = swallowInteraction, indication = null, onClick = {})
                    .padding(vertical = 26.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "WANT TO RETRY?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    RoundIconButton(background = Color.White, icon = Icons.Filled.Close, contentDescription = "Cancel", contentColor = AccentGold, size = 50.dp, onClick = onCancel)
                    RoundIconButton(background = Color.White, icon = Icons.Filled.Check, contentDescription = "Confirm", contentColor = AccentGold, size = 50.dp, onClick = onConfirm)
                }
            }
        }
    }
}

private val CAUGHT_CAPTIONS = listOf(
    "A guard caught you — give it another shot!",
    "Busted. The guard is extremely proud of themselves right now.",
    "Caught red-handed. Or red-pixeled.",
    "Well, that escalated quickly.",
    "The guard really said 'gotcha' and meant it.",
    "Skill issue. Respawn and cook."
)

@Composable
private fun CaughtOverlay(playerPhoto: ImageBitmap?, onRetry: () -> Unit, onHome: () -> Unit) {
    val caption = remember { CAUGHT_CAPTIONS.random() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            // Same pass-through fix — once caught, the board underneath must
            // be fully inert until RETRY/HOME is tapped.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .background(BackgroundBottom)
                .border(width = 2.dp, color = EnemyColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JailBarsStrip(modifier = Modifier
                .fillMaxWidth()
                .height(14.dp))

            Spacer(Modifier.height(16.dp))
            JailedAvatar(photo = playerPhoto, modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(16.dp))

            Text(
                "CAUGHT",
                color = EnemyColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                caption,
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OverlayButton(text = "TRY AGAIN", background = EnemyColor, onClick = onRetry)
                OverlayButton(text = "HOME", background = CardLocked, textColor = TextPrimary, onClick = onHome)
            }
        }
    }
}

/**
 * The player's own in-game photo, dimmed and crosshatched with a net
 * pattern, ringed in the guard-danger color — "caught in a net" instead of
 * a generic sad-face icon. Plain Canvas drawing, no gradients/blur, in
 * keeping with the rest of the app's flat look.
 */
@Composable
private fun JailedAvatar(photo: ImageBitmap?, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = minOf(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val diameter = radius * 2f

        if (photo != null) {
            drawImage(
                image = photo,
                dstOffset = IntOffset((center.x - radius).roundToInt(), (center.y - radius).roundToInt()),
                dstSize = IntSize(diameter.roundToInt(), diameter.roundToInt())
            )
        } else {
            drawCircle(color = CardLocked, radius = radius, center = center)
        }

        // Dim the photo so it reads as "caught", then crosshatch a net over it.
        drawCircle(color = Color.Black.copy(alpha = 0.32f), radius = radius, center = center)

        clipPath(Path().apply {
            addOval(Rect(offset = Offset(center.x - radius, center.y - radius), size = Size(diameter, diameter)))
        }) {
            val netColor = Color.White.copy(alpha = 0.9f)
            val strokeWidth = radius * 0.055f
            val spacing = radius * 0.5f
            val span = radius * 1.5f
            var offset = -span
            while (offset < span) {
                drawLine(
                    color = netColor,
                    start = Offset(center.x - span, center.y + offset),
                    end = Offset(center.x + span, center.y + offset - span * 2f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = netColor,
                    start = Offset(center.x - span, center.y + offset - span * 2f),
                    end = Offset(center.x + span, center.y + offset),
                    strokeWidth = strokeWidth
                )
                offset += spacing
            }
        }

        drawCircle(color = EnemyColor, radius = radius, center = center, style = Stroke(width = radius * 0.09f))
    }
}

/** A plain row of thick jail-bar rectangles — cheap flat shapes, no gradient/shadow. */
@Composable
private fun JailBarsStrip(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        repeat(9) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(EnemyColor)
            )
        }
    }
}

@Composable
private fun LockedLevelNotice(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BackgroundBottom)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("HOLD UP", color = AccentAmber, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "That maze isn't built yet — clear this one first, speedrunner.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OverlayButton(
    text: String,
    background: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
