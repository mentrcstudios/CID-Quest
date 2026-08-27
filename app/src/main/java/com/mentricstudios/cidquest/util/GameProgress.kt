package com.mentricstudios.cidquest.util

import android.content.Context

private const val PREFS_NAME = "cid_quest_progress"

/**
 * Tracks which levels the player has cleared at least once, persisted
 * locally via SharedPreferences so progress survives app restarts. Keyed by
 * "<category>_<levelNumber>". No scoring — a level is either cleared or it
 * isn't, which is all Level Select needs to decide what to unlock next.
 */
object GameProgress {

    private fun key(category: String, levelNumber: Int) = "cleared_${category}_$levelNumber"

    fun isCompleted(context: Context, category: String, levelNumber: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(key(category, levelNumber), false)
    }

    /** Marks a level cleared. Returns true the first time it's ever cleared. */
    fun markCompleted(context: Context, category: String, levelNumber: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val k = key(category, levelNumber)
        val wasAlreadyCleared = prefs.getBoolean(k, false)
        if (!wasAlreadyCleared) {
            prefs.edit().putBoolean(k, true).apply()
        }
        return !wasAlreadyCleared
    }

    /** How many levels in [levels] have been cleared at least once — for labels like "1/1". */
    fun completedCount(context: Context, levels: List<com.mentricstudios.cidquest.game.MazeLevel>): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return levels.count { prefs.getBoolean(key(it.category, it.levelNumber), false) }
    }
}
