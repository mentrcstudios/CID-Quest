package com.mentricstudios.cidquest.util

import android.content.Context
import com.mentricstudios.cidquest.game.MazeLevel
import com.mentricstudios.cidquest.game.MazeLevels

private const val PREFS_NAME = "cid_quest_progress"

/**
 * Tracks the player's best star rating on every level they've cleared, persisted
 * locally via SharedPreferences so progress survives app restarts. Keyed by
 * "<category>_<levelNumber>". Only the best-ever result per level is kept, so
 * replaying a level can never make your recorded progress go down.
 */
object GameProgress {

    private fun key(category: String, levelNumber: Int) = "stars_${category}_$levelNumber"

    fun bestStars(context: Context, category: String, levelNumber: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(key(category, levelNumber), 0)
    }

    /**
     * Records a level result, keeping whichever star rating is best across all
     * attempts. Returns true if this particular run improved the previous best
     * (handy for showing a "New Best!" callout later).
     */
    fun recordResult(context: Context, category: String, levelNumber: Int, stars: Int): Boolean {
        if (stars <= 0) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val k = key(category, levelNumber)
        val previousBest = prefs.getInt(k, 0)
        if (stars > previousBest) {
            prefs.edit().putInt(k, stars).apply()
            return true
        }
        return false
    }

    /**
     * Total stars earned across every level that exists in the game right now,
     * plus any bonus stars claimed from the daily-streak reward (see
     * [DailyRewards]) — both currencies feed the same star counter everywhere.
     */
    fun totalStars(context: Context, levels: List<MazeLevel> = MazeLevels.CLASSIC + MazeLevels.ICE_FLOOR + MazeLevels.DARKNESS): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val levelStars = levels.sumOf { level -> prefs.getInt(key(level.category, level.levelNumber), 0) }
        return levelStars + DailyRewards.bonusStars(context)
    }

    /** How many levels in [levels] have been cleared at least once — for labels like "1/1". */
    fun completedCount(context: Context, levels: List<MazeLevel>): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return levels.count { prefs.getInt(key(it.category, it.levelNumber), 0) > 0 }
    }
}
