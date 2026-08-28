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
     * maze; touching one resets the level. Levels 1-2 are hand-tuned
     * intros; levels 3-50 are procedurally scaled below so the full
     * 50-level set (matching [ENEMIES_TOTAL_LEVELS]) is always playable.
     */
    val ENEMIES: List<MazeLevel> = buildList {
        add(
            MazeLevel(
                category = "Enemies",
                levelNumber = 1,
                rows = 9,
                cols = 6,
                seed = 50001L,
                maxHints = 2,
                enemies = listOf(
                    EnemyPatrol(from = CellPos(1, 2), to = CellPos(6, 2), stepMillis = 460)
                )
            )
        )
        // Level 2: bigger grid and a second guard on an independent patrol
        // route, so the player has to track two moving threats at once.
        add(
            MazeLevel(
                category = "Enemies",
                levelNumber = 2,
                rows = 10,
                cols = 6,
                seed = 50002L,
                maxHints = 2,
                enemies = listOf(
                    EnemyPatrol(from = CellPos(1, 1), to = CellPos(8, 1), stepMillis = 420),
                    EnemyPatrol(from = CellPos(2, 4), to = CellPos(7, 4), stepMillis = 400)
                )
            )
        )

        // Levels 3-50: grid grows the same way every level or two (capped at
        // 26x15 so late levels stay readable), guard count climbs from 2 up
        // to 6, and guards get faster (lower stepMillis, floored at 230ms so
        // it never becomes unfair) as the level number rises. Guards
        // alternate between vertical and horizontal patrol lanes and are
        // spaced evenly across the grid so they never clump together.
        for (levelNumber in 3..ENEMIES_TOTAL_LEVELS) {
            val rows = (9 + levelNumber).coerceAtMost(26)
            val cols = (6 + (levelNumber * 6) / 10).coerceAtMost(15)
            val guardCount = when {
                levelNumber <= 8 -> 2
                levelNumber <= 16 -> 3
                levelNumber <= 26 -> 4
                levelNumber <= 38 -> 5
                else -> 6
            }
            val stepMillis = (460 - levelNumber * 4).coerceAtLeast(230)
            val maxHints = if (levelNumber <= 6) 2 else 1

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
