package com.mentricstudios.cidquest.game

/**
 * Central catalog of playable levels. Adding a new level is just adding one
 * entry here — rows/cols control size & difficulty, seed controls the exact
 * layout (change it to get a different — but still guaranteed solvable —
 * maze of the same size).
 */
object MazeLevels {

    /** Total tiles shown on the level-select grid. */
    const val ENEMIES_TOTAL_LEVELS = 50

    /**
     * Red patrol-guards walking fixed back-and-forth routes through the
     * maze — closing in fast once they spot you — touching one resets the
     * level. All 50 levels come from one formula below, not hand-tuned
     * individually: guard count, grid size, guard speed, and how many
     * escape-route loops the maze gets all scale with [levelNumber]
     * together. (Levels 1-2 used to be hand-tuned separately from the
     * curve, which is exactly how level 2 ended up harder than levels
     * 3-8 — one shared formula can't drift out of sync with itself.)
     *
     * Guard count: 1 for levels 1-2, 2 for levels 3-4, 3 for levels 5-14,
     * 4 for levels 15-26, 5 for levels 27-40, 6 for levels 41-50 — so the
     * jump to 3 simultaneous guards (previously as early as level 2) now
     * lands at level 5, with two full guard-count steps of gentle ramp-up
     * ahead of it instead of a sudden spike right after the tutorial level.
     */
    val ENEMIES: List<MazeLevel> = buildList {
        for (levelNumber in 1..ENEMIES_TOTAL_LEVELS) {
            val rows = (9 + levelNumber).coerceAtMost(26)
            val cols = (6 + (levelNumber * 6) / 10).coerceAtMost(15)
            val guardCount = when {
                levelNumber <= 2 -> 1
                levelNumber <= 4 -> 2
                levelNumber <= 14 -> 3
                levelNumber <= 26 -> 4
                levelNumber <= 40 -> 5
                else -> 6
            }
            val stepMillis = (460 - levelNumber * 4).coerceAtLeast(230)
            val maxHints = if (levelNumber <= 8) 2 else 1
            // Extra-generous escape routes through the early and
            // just-ramped-up stretches, settling to the standard density
            // once the player's had a few levels to get used to a guard
            // count before the next one arrives.
            val braidChance = when {
                levelNumber <= 4 -> 0.22
                levelNumber <= 14 -> 0.20
                else -> 0.16
            }

            val vSlots = (guardCount + 1) / 2
            val hSlots = guardCount / 2
            val enemies = (0 until guardCount).map { g ->
                val lane = g / 2
                if (g % 2 == 0) {
                    val col = (1 + (lane + 1) * (cols - 2) / (vSlots + 1)).coerceIn(1, cols - 2)
                    EnemyPatrol(from = CellPos(1, col), to = CellPos(rows - 2, col), stepMillis = stepMillis)
                } else {
                    val row = (1 + (lane + 1) * (rows - 2) / (hSlots + 1)).coerceIn(1, rows - 2)
                    EnemyPatrol(from = CellPos(row, 1), to = CellPos(row, cols - 2), stepMillis = stepMillis)
                }
            }

            add(
                MazeLevel(
                    category = "Enemies",
                    levelNumber = levelNumber,
                    rows = rows,
                    cols = cols,
                    seed = 50000L + levelNumber,
                    maxHints = maxHints,
                    braidChance = braidChance,
                    enemies = enemies
                )
            )
        }
    }

    fun find(category: String, levelNumber: Int): MazeLevel? =
        when (category) {
            "Enemies" -> ENEMIES.firstOrNull { it.levelNumber == levelNumber }
            else -> null
        }

    fun isPlayable(category: String, levelNumber: Int): Boolean =
        find(category, levelNumber) != null
}
