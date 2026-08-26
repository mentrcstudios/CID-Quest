package com.mentricstudios.cidquest.game

import androidx.compose.ui.graphics.Color
import com.mentricstudios.cidquest.ui.theme.CategoryClassic

/** Which shop shelf a skin belongs to. */
enum class SkinType { PLAYER }

/**
 * A single unlockable cosmetic. The hero/guard shapes stay the same — only
 * their color changes — so unlocking a skin never needs new art, just stars.
 * [cost] of 0 means it's the default, already owned by everyone.
 *
 * [gradientColors], when present, marks this as one of the premium "fancy"
 * skins — the shop swatch renders a multi-color gradient glow instead of a
 * flat dot. Gameplay still renders the maze token in a single solid color
 * (perfectly readable at that tiny size), so [color] stays the dominant hue
 * used in-game; the gradient is a shop-only flourish.
 */
data class Skin(
    val id: String,
    val displayName: String,
    val color: Color,
    val cost: Int,
    val gradientColors: List<Color>? = null
)

object SkinsCatalog {

    const val DEFAULT_PLAYER_SKIN_ID = "spark"

    val PLAYER_SKINS = listOf(
        Skin(id = "spark", displayName = "Spark Spirit", color = CategoryClassic, cost = 0),
        Skin(id = "ember", displayName = "Ember", color = Color(0xFFFF7A33), cost = 40),
        Skin(id = "violet", displayName = "Violet Wisp", color = Color(0xFFB026FF), cost = 45),
        Skin(id = "mint", displayName = "Mint Frost", color = Color(0xFF2CFFC6), cost = 60),
        Skin(id = "gold", displayName = "Gold Rush", color = Color(0xFFFFD23F), cost = 75),
        Skin(id = "rose", displayName = "Rose Quartz", color = Color(0xFFFF6FB0), cost = 90),
        Skin(id = "lime", displayName = "Lime Bolt", color = Color(0xFFAEFF33), cost = 110),
        Skin(id = "crimson", displayName = "Crimson", color = Color(0xFFFF2D55), cost = 130),
        Skin(
            id = "aurora", displayName = "Aurora Drift", color = Color(0xFF2CFFC6), cost = 170,
            gradientColors = listOf(Color(0xFF2CFFC6), Color(0xFFB026FF), Color(0xFFFF6FB0))
        ),
        Skin(
            id = "solar", displayName = "Solar Flare", color = Color(0xFFFFD23F), cost = 210,
            gradientColors = listOf(Color(0xFFFFD23F), Color(0xFFFF7A33), Color(0xFFFF2D55))
        ),
        Skin(
            id = "galaxy", displayName = "Galaxy Core", color = Color(0xFFB026FF), cost = 260,
            gradientColors = listOf(Color(0xFF3F2CFF), Color(0xFFB026FF), Color(0xFFFF6FB0))
        )
    )

    fun skinsFor(type: SkinType): List<Skin> = when (type) {
        SkinType.PLAYER -> PLAYER_SKINS
    }

    fun defaultIdFor(type: SkinType): String = when (type) {
        SkinType.PLAYER -> DEFAULT_PLAYER_SKIN_ID
    }

    fun colorFor(type: SkinType, id: String): Color =
        skinsFor(type).firstOrNull { it.id == id }?.color ?: skinsFor(type).first().color
}
