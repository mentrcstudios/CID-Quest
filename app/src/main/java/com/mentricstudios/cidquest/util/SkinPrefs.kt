package com.mentricstudios.cidquest.util

import android.content.Context
import com.mentricstudios.cidquest.game.SkinType
import com.mentricstudios.cidquest.game.SkinsCatalog

private const val PREFS_NAME = "cid_quest_skins"
private const val KEY_SELECTED_PLAYER = "selected_player_skin"

/**
 * Skins unlock automatically as the player earns stars — there's no separate
 * "purchase" step and stars are never spent. A skin simply becomes available
 * the instant the player's total star count reaches its cost, so this object
 * only needs to remember which of the already-unlocked skins is equipped.
 */
object SkinPrefs {

    private fun selectedKey(type: SkinType) = KEY_SELECTED_PLAYER

    fun selectedSkinId(context: Context, type: SkinType): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(selectedKey(type), SkinsCatalog.defaultIdFor(type))
            ?: SkinsCatalog.defaultIdFor(type)
        // Guard against a previously-equipped skin that isn't unlocked anymore
        // (e.g. stars dropped, or the catalog changed) — fall back to the
        // always-free default instead of silently wearing it anyway.
        return if (isUnlocked(context, type, stored)) stored else SkinsCatalog.defaultIdFor(type)
    }

    fun select(context: Context, type: SkinType, skinId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(selectedKey(type), skinId).apply()
    }

    /** A skin is unlocked the moment total stars earned reach its cost — nothing to buy. */
    fun isUnlocked(context: Context, type: SkinType, skinId: String): Boolean {
        val skin = SkinsCatalog.skinsFor(type).firstOrNull { it.id == skinId } ?: return false
        return totalStars(context) >= skin.cost
    }

    fun totalStars(context: Context): Int = GameProgress.totalStars(context)
}
