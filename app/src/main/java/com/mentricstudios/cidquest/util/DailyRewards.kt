package com.mentricstudios.cidquest.util

import android.content.Context
import java.util.Calendar

private const val PREFS_NAME = "cid_quest_daily"
private const val KEY_LAST_CLAIM_DAY = "last_claim_epoch_day"
private const val KEY_STREAK = "streak_count"
private const val KEY_BONUS_STARS = "bonus_stars"

/**
 * Classic "come back tomorrow" daily-streak reward. Claiming resets to day 1
 * of a fresh 7-day cycle if the player misses a day, or advances the streak
 * if they claimed yesterday — this is the single biggest lever for turning a
 * one-off maze-solver into a habit, so it's worth having even before the
 * rest of the meta-game (shop, leaderboard, etc.) exists.
 *
 * Uses local-midnight-aligned day numbers via [Calendar] rather than
 * java.time, since this project's minSdk (24) predates java.time without
 * core-library desugaring.
 */
object DailyRewards {

    /** Stars awarded on each day of the 7-day cycle (index 0 = day 1). */
    private val CYCLE_REWARDS = intArrayOf(5, 5, 10, 10, 15, 15, 30)

    private fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis / 86_400_000L
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** True once per calendar day — false again the instant local midnight passes. */
    fun canClaimToday(context: Context): Boolean {
        val lastDay = prefs(context).getLong(KEY_LAST_CLAIM_DAY, Long.MIN_VALUE)
        return lastDay != todayEpochDay()
    }

    /** Current streak length (1-7), reflecting the most recent successful claim. */
    fun currentStreak(context: Context): Int = prefs(context).getInt(KEY_STREAK, 0)

    /** What the streak (and reward) would become if the player claims right now. */
    fun pendingStreakDay(context: Context): Int {
        val p = prefs(context)
        val lastDay = p.getLong(KEY_LAST_CLAIM_DAY, Long.MIN_VALUE)
        val prevStreak = p.getInt(KEY_STREAK, 0)
        return if (lastDay == todayEpochDay() - 1 && prevStreak in 1..6) prevStreak + 1 else 1
    }

    fun rewardForDay(day: Int): Int = CYCLE_REWARDS[(day - 1).coerceIn(0, CYCLE_REWARDS.size - 1)]

    /**
     * Claims today's reward if not already claimed. Returns the number of
     * bonus stars just awarded, or null if today was already claimed.
     */
    fun claim(context: Context): Int? {
        if (!canClaimToday(context)) return null
        val day = pendingStreakDay(context)
        val reward = rewardForDay(day)
        prefs(context).edit()
            .putLong(KEY_LAST_CLAIM_DAY, todayEpochDay())
            .putInt(KEY_STREAK, day)
            .putInt(KEY_BONUS_STARS, prefs(context).getInt(KEY_BONUS_STARS, 0) + reward)
            .apply()
        return reward
    }

    /** Bonus stars earned from daily claims, separate from per-level star ratings. */
    fun bonusStars(context: Context): Int = prefs(context).getInt(KEY_BONUS_STARS, 0)

    /**
     * Adds [amount] stars to the same bonus pool as the daily claim — used by
     * rewarded-ad placements (e.g. "watch an ad for +stars") so every star
     * source feeds the one counter shown everywhere ([GameProgress.totalStars]).
     */
    fun grantBonusStars(context: Context, amount: Int) {
        if (amount <= 0) return
        val p = prefs(context)
        p.edit().putInt(KEY_BONUS_STARS, p.getInt(KEY_BONUS_STARS, 0) + amount).apply()
    }
}
