package com.mentricstudios.cidquest.ads

import android.content.Context

private const val PREFS_NAME = "cid_quest_ad_frequency"
private const val KEY_LEVEL_TRANSITIONS = "level_transitions_since_last_interstitial"

/**
 * Caps how often the level-transition interstitial shows, so it's "there's
 * an ad now and then" rather than one on every single Home/Next tap. Counts
 * transitions since the last interstitial and only allows a show every
 * [SHOW_EVERY] of them.
 *
 * Split into peek/record instead of one combined check-and-consume: the
 * counter must only advance to "shown" when an interstitial actually
 * displayed. If we consumed the slot the moment a transition was merely
 * *eligible* — before confirming an ad was preloaded and ready — a slow or
 * failed ad load would silently burn that eligible turn with nothing shown,
 * pushing the next real impression another [SHOW_EVERY] transitions out.
 * Over a session that quietly starves real ad delivery well below the
 * intended rate, which is the actual cause of "ads show too rarely" — not
 * [SHOW_EVERY] itself.
 */
object AdFrequency {
    private const val SHOW_EVERY = 2

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Call on every level-complete/leave-game transition to check eligibility
     * WITHOUT consuming it. Only [recordTransition] with `interstitialShown =
     * true` should reset the counter — call this first, then only pass true
     * if [AdsManager] actually had a preloaded ad ready to show. */
    fun isInterstitialEligible(context: Context): Boolean =
        prefs(context).getInt(KEY_LEVEL_TRANSITIONS, 0) + 1 >= SHOW_EVERY

    /** Call once an interstitial actually finished showing (or was skipped
     * because nothing was ready) to advance the counter for next time. */
    fun recordTransition(context: Context, interstitialShown: Boolean) {
        val p = prefs(context)
        if (interstitialShown) {
            p.edit().putInt(KEY_LEVEL_TRANSITIONS, 0).apply()
        } else {
            val count = p.getInt(KEY_LEVEL_TRANSITIONS, 0) + 1
            p.edit().putInt(KEY_LEVEL_TRANSITIONS, count).apply()
        }
    }
}
