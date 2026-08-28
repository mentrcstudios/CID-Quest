package com.mentricstudios.cidquest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mentricstudios.cidquest.ads.AdsManager
import com.mentricstudios.cidquest.game.MazeLevels
import com.mentricstudios.cidquest.navigation.Routes
import com.mentricstudios.cidquest.notifications.NotificationChannels
import com.mentricstudios.cidquest.notifications.ReminderScheduler
import com.mentricstudios.cidquest.screens.AgeGateScreen
import com.mentricstudios.cidquest.screens.HomeScreen
import com.mentricstudios.cidquest.screens.LoadingScreen
import com.mentricstudios.cidquest.screens.MazeGameScreen
import com.mentricstudios.cidquest.screens.StudioSplashScreen
import com.mentricstudios.cidquest.ui.theme.CidQuestTheme
import com.mentricstudios.cidquest.util.GameProgress
import com.mentricstudios.cidquest.util.NotificationPrefs
import com.mentricstudios.cidquest.util.OnboardingPrefs

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Whether granted or denied, arm/skip the schedule accordingly —
        // ReminderReceiver itself also re-checks the permission before
        // posting, so a later denial in system settings is respected too.
        ReminderScheduler.scheduleDailyReminders(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdsManager.initialize(this)
        NotificationChannels.createChannels(this)

        // Only prompt for the runtime permission once onboarding (Age gate)
        // is already behind the player. There's no Settings screen to turn
        // reminders off anymore, so this just always tries to schedule them
        // — NotificationPrefs still defaults to enabled.
        if (OnboardingPrefs.isOnboardingComplete(this) && NotificationPrefs.areRemindersEnabled(this)) {
            ensureNotificationPermissionThenSchedule()
        }

        setContent {
            CidQuestTheme {
                CidQuestApp(onOnboardingJustCompleted = { ensureNotificationPermissionThenSchedule() })
            }
        }
    }

    private fun ensureNotificationPermissionThenSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                ReminderScheduler.scheduleDailyReminders(this)
            } else {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            ReminderScheduler.scheduleDailyReminders(this)
        }
    }
}

@Composable
fun CidQuestApp(onOnboardingJustCompleted: () -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.STUDIO_SPLASH
        // No custom enter/exit transitions — plain instant screen switches.
        // Cheaper to render (no animation running every navigation) and
        // matches the plain, low-budget feel the rest of the UI is going for.
    ) {

        composable(Routes.STUDIO_SPLASH) {
            StudioSplashScreen(onFinished = {
                navController.navigate(Routes.LOADING) {
                    popUpTo(Routes.STUDIO_SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.LOADING) {
            LoadingScreen(onFinished = {
                val nextRoute = if (OnboardingPrefs.isOnboardingComplete(context)) {
                    Routes.HOME
                } else {
                    Routes.AGE_GATE
                }
                navController.navigate(nextRoute) {
                    popUpTo(Routes.LOADING) { inclusive = true }
                }
            })
        }

        composable(Routes.AGE_GATE) {
            AgeGateScreen(onConfirmed = {
                OnboardingPrefs.setOnboardingComplete(context)
                onOnboardingJustCompleted()
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.AGE_GATE) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                onPlay = {
                    // No level-select screen — Play always drops the player
                    // straight into wherever they're up to: the first level
                    // they haven't cleared yet (or the last one, to replay,
                    // once everything's cleared).
                    val category = "Enemies"
                    val nextLevel = GameProgress.nextLevelToPlay(context, category, MazeLevels.ENEMIES_TOTAL_LEVELS)
                    navController.navigate(Routes.game(category, nextLevel))
                }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType },
                navArgument("levelNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Enemies"
            val levelNumber = backStackEntry.arguments?.getInt("levelNumber") ?: 1
            MazeGameScreen(
                category = categoryName,
                levelNumber = levelNumber,
                onHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNextLevel = { nextLevelNumber ->
                    navController.navigate(Routes.game(categoryName, nextLevelNumber))
                }
            )
        }
    }
}
