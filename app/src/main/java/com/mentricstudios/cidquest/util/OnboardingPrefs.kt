package com.mentricstudios.cidquest.util

import android.content.Context

private const val PREFS_NAME = "cid_quest_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"
private const val KEY_GAME_TUTORIAL_DONE = "game_tutorial_done"

object OnboardingPrefs {
    fun isOnboardingComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingComplete(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    // Separate from the Terms/Age onboarding flag above — this tracks
    // whether the player has already been walked through the in-game
    // "how to play" coach marks (swipe, hint, pause/stars), so it's only
    // ever shown automatically once, the very first time someone opens a
    // level, on any device the app has been used on.
    fun hasSeenGameTutorial(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_GAME_TUTORIAL_DONE, false)
    }

    fun setGameTutorialSeen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_GAME_TUTORIAL_DONE, true).apply()
    }
}
