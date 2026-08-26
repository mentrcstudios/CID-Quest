package com.mentricstudios.cidquest.game

import kotlin.random.Random

/**
 * Central catalog of playable levels. Adding a new level is just adding one
 * entry here — rows/cols control size & difficulty, seed controls the exact
 * layout (change it to get a different — but still guaranteed solvable —
 * maze of the same size).
 */
object MazeLevels {

    val CLASSIC = listOf(
        MazeLevel(category = "Classic", levelNumber = 1, rows = 9, cols = 6, seed = 1001L, maxHints = 2),
        // Level 2: noticeably bigger grid (more cells, more branching, longer
        // optimal path) than level 1, but keeps the same hint allowance so it
        // stays fair — the extra challenge comes purely from maze size/shape.
        MazeLevel(category = "Classic", levelNumber = 2, rows = 11, cols = 7, seed = 2002L, maxHints = 2),
        // Levels 3-10: grid keeps growing every level or two, and hints get
        // tighter from level 5 onward — the difficulty ramp is gradual so no
        // single level feels like a wall, but level 10 is a real challenge.
        MazeLevel(category = "Classic", levelNumber = 3, rows = 12, cols = 7, seed = 3003L, maxHints = 2),
        MazeLevel(category = "Classic", levelNumber = 4, rows = 13, cols = 8, seed = 4004L, maxHints = 2),
        MazeLevel(category = "Classic", levelNumber = 5, rows = 14, cols = 8, seed = 5005L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 6, rows = 15, cols = 9, seed = 6006L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 7, rows = 16, cols = 9, seed = 7007L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 8, rows = 17, cols = 10, seed = 8008L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 9, rows = 18, cols = 10, seed = 9009L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 10, rows = 20, cols = 11, seed = 10010L, maxHints = 1),
        // Levels 11-30: size keeps creeping up for a while, then plateaus around
        // 26x15 for the last stretch — past that point a bigger grid stops adding
        // fun and just adds scrolling/squinting, so later "expert" levels get
        // their challenge from a fresh maze shape (new seed) rather than raw size.
        MazeLevel(category = "Classic", levelNumber = 11, rows = 20, cols = 12, seed = 11011L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 12, rows = 21, cols = 12, seed = 12012L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 13, rows = 21, cols = 13, seed = 13013L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 14, rows = 22, cols = 13, seed = 14014L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 15, rows = 22, cols = 13, seed = 15015L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 16, rows = 23, cols = 14, seed = 16016L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 17, rows = 23, cols = 14, seed = 17017L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 18, rows = 24, cols = 14, seed = 18018L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 19, rows = 24, cols = 14, seed = 19019L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 20, rows = 24, cols = 15, seed = 20020L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 21, rows = 25, cols = 15, seed = 21021L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 22, rows = 25, cols = 15, seed = 22022L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 23, rows = 25, cols = 15, seed = 23023L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 24, rows = 26, cols = 15, seed = 24024L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 25, rows = 26, cols = 15, seed = 25025L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 26, rows = 26, cols = 15, seed = 26026L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 27, rows = 26, cols = 15, seed = 27027L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 28, rows = 26, cols = 15, seed = 28028L, maxHints = 1),
        MazeLevel(category = "Classic", levelNumber = 29, rows = 26, cols = 15, seed = 29029L, maxHints = 1),
        // Level 30 — the final Classic maze: biggest, most winding layout in the set.
        MazeLevel(category = "Classic", levelNumber = 30, rows = 26, cols = 15, seed = 30030L, maxHints = 1)
    )

    /**
     * Ice Floor category — frictionless movement: a swipe sends the player
     * sliding the whole way in that direction, stopping only when a real
     * wall blocks the next cell (see [com.mentricstudios.cidquest.screens.MazeGameScreen]'s
     * ice-mode `tryMove`), never at a mid-corridor intersection.
     *
     * Pure route-planning under the slide constraint is the whole challenge
     * here: levels 1-11 are hand-tuned (board keeps growing, hint budget
     * tightens from 2 down to 1 by level 5), then 12-70 continue the same
     * size curve procedurally, capped at 26x15 so late boards stay readable.
     */
    const val ICE_FLOOR_TOTAL_LEVELS = 70

    val ICE_FLOOR: List<MazeLevel> = buildList {
        // 1: First slide — small, single corridor-y board.
        add(MazeLevel(category = "Ice Floor", levelNumber = 1, rows = 8, cols = 6, seed = 70001L, maxHints = 2))
        // 2: Longer runs — a bit more room for the slide to travel.
        add(MazeLevel(category = "Ice Floor", levelNumber = 2, rows = 9, cols = 6, seed = 70002L, maxHints = 2))
        // 3: Tighter turns — more junctions per slide, same hint budget.
        add(MazeLevel(category = "Ice Floor", levelNumber = 3, rows = 10, cols = 7, seed = 70003L, maxHints = 2))
        add(MazeLevel(category = "Ice Floor", levelNumber = 4, rows = 11, cols = 7, seed = 70004L, maxHints = 2))
        // 5: Hint budget drops to 1 on a bigger board — pure route planning
        // under the frictionless-slide constraint.
        add(MazeLevel(category = "Ice Floor", levelNumber = 5, rows = 12, cols = 8, seed = 70005L, maxHints = 1))
        add(MazeLevel(category = "Ice Floor", levelNumber = 6, rows = 13, cols = 8, seed = 70006L, maxHints = 1))
        add(MazeLevel(category = "Ice Floor", levelNumber = 7, rows = 14, cols = 9, seed = 70007L, maxHints = 1))
        // 8: Biggest "pure maze" board in the intro arc — tests whether the
        // player can track a long slide route from memory alone.
        add(MazeLevel(category = "Ice Floor", levelNumber = 8, rows = 15, cols = 9, seed = 70008L, maxHints = 1))
        add(MazeLevel(category = "Ice Floor", levelNumber = 9, rows = 16, cols = 10, seed = 70009L, maxHints = 1))
        add(MazeLevel(category = "Ice Floor", levelNumber = 10, rows = 17, cols = 10, seed = 70010L, maxHints = 1))
        // 11: Hand-tuned finale of the intro arc — biggest board yet, one hint.
        add(MazeLevel(category = "Ice Floor", levelNumber = 11, rows = 18, cols = 11, seed = 70011L, maxHints = 1))

        // Levels 12-70: continue the same size formula as 1-11 (rows =
        // 7+levelNumber, cols = 6+(levelNumber-1)/2), capped at 26x15 so
        // late boards stay readable — the rest of the category's difficulty
        // comes purely from the board getting bigger.
        for (levelNumber in 12..ICE_FLOOR_TOTAL_LEVELS) {
            val rows = (7 + levelNumber).coerceAtMost(26)
            val cols = (6 + (levelNumber - 1) / 2).coerceAtMost(15)
            add(MazeLevel(category = "Ice Floor", levelNumber = levelNumber, rows = rows, cols = cols, seed = 70000L + levelNumber, maxHints = 1))
        }
    }

    fun find(category: String, levelNumber: Int): MazeLevel? =
        when (category) {
            "Classic" -> CLASSIC.firstOrNull { it.levelNumber == levelNumber }
            "Ice Floor" -> ICE_FLOOR.firstOrNull { it.levelNumber == levelNumber }
            "Darkness" -> DARKNESS.firstOrNull { it.levelNumber == levelNumber }
            else -> null
        }

    /** Total tiles shown on the Darkness level-select grid. */
    const val DARKNESS_TOTAL_LEVELS = 50

    /**
     * Deterministically scatters [count] wisp pickups across a rows x cols
     * grid, seeded so the same level always gets the same placement. Since
     * a perfect maze has exactly one route between any two cells, every
     * cell is reachable no matter what the generated layout looks like —
     * the only thing worth avoiding is dropping a wisp right on the start
     * or goal cell, where it'd be trivial or pointless to grab.
     */
    private fun scatteredWisps(rows: Int, cols: Int, count: Int, seed: Long): List<CellPos> {
        if (count <= 0 || rows < 3 || cols < 3) return emptyList()
        val rnd = Random(seed)
        val start = CellPos(0, 0)
        val goal = CellPos(rows - 1, cols - 1)
        val picked = LinkedHashSet<CellPos>()
        var guard = 0
        while (picked.size < count && guard < count * 30) {
            guard++
            val pos = CellPos(rnd.nextInt(rows), rnd.nextInt(cols))
            if (pos != start && pos != goal) picked.add(pos)
        }
        return picked.toList()
    }

    /**
     * Darkness category — a real-time fog-of-war instead of the "reveal once,
     * stays revealed" wave every other category uses. Only a small radius of
     * light around the player is ever drawn; step away and the corridor goes
     * black again. See [com.mentricstudios.cidquest.screens.MazeGameScreen]'s
     * `MazeBoard` for the actual masking, which layers a few ideas beyond a
     * plain moving spotlight:
     *   - Memory trail: cells you've already stood near stay sketched in as
     *     a faint, dim outline even after your light moves on, so you build
     *     a real mental map instead of forgetting everything you just saw.
     *   - Flicker: the light's edge isn't a perfect circle — it breathes
     *     unevenly, like a real torch, so the safe radius is never quite
     *     the same size twice.
     *   - Distant beacon: the goal is never fully hidden — a faint glow of
     *     it is always visible, brightening as you close in, so darkness
     *     adds tension without making the exit feel unfair to find.
     *
     * Levels 1-10 tune the fog itself (radius shrinks a notch each level).
     * From there the arc keeps going to 50 with two more ideas layered in
     * once the fog is second nature — see [MazeLevel.wisps] and
     * [MazeLevel.hasGusts] for what each one does mechanically:
     *   11-25  Wisp pickups appear for the first time — small collectible
     *          motes that permanently widen the torch a notch when walked
     *          over, so a level is no longer just "navigate the fog", it's
     *          "is a detour for a bit more light worth it".
     *   26-50  Gusts join on top of wisps — every so often the torch
     *          gutters down to a sliver for about a second before
     *          recovering, forcing a brief "freeze and trust the memory
     *          trail" beat instead of only ever reading live light. Radius
     *          keeps shrinking toward its floor all the way to the finale.
     *   50     Hand-tuned finale: smallest torch in the set, gusts active,
     *          wisps scattered for anyone willing to detour.
     */
    val DARKNESS: List<MazeLevel> = buildList {
        // 1: First torch — generous radius, tiny board, just learning to
        // trust the memory trail instead of the whole map.
        add(MazeLevel(category = "Darkness", levelNumber = 1, rows = 8, cols = 6, seed = 80001L, maxHints = 2, visionRadius = 2.6f))
        // 2: Radius trimmed a touch, slightly bigger board.
        add(MazeLevel(category = "Darkness", levelNumber = 2, rows = 9, cols = 7, seed = 80002L, maxHints = 2, visionRadius = 2.4f))
        // 3: Another notch down — the memory trail starts actually mattering
        // for backtracking instead of being a nice-to-have.
        add(MazeLevel(category = "Darkness", levelNumber = 3, rows = 10, cols = 7, seed = 80003L, maxHints = 2, visionRadius = 2.2f))
        // 4: Hint budget drops to 1; radius keeps shrinking on a bigger board.
        add(MazeLevel(category = "Darkness", levelNumber = 4, rows = 11, cols = 8, seed = 80004L, maxHints = 1, visionRadius = 2.0f))
        // 5: Smallest radius yet — pure fog navigation.
        add(MazeLevel(category = "Darkness", levelNumber = 5, rows = 12, cols = 8, seed = 80005L, maxHints = 1, visionRadius = 1.9f))
        add(MazeLevel(category = "Darkness", levelNumber = 6, rows = 13, cols = 9, seed = 80006L, maxHints = 1, visionRadius = 1.8f))
        add(MazeLevel(category = "Darkness", levelNumber = 7, rows = 14, cols = 9, seed = 80007L, maxHints = 1, visionRadius = 1.7f))
        add(MazeLevel(category = "Darkness", levelNumber = 8, rows = 15, cols = 10, seed = 80008L, maxHints = 1, visionRadius = 1.6f))
        add(MazeLevel(category = "Darkness", levelNumber = 9, rows = 16, cols = 10, seed = 80009L, maxHints = 1, visionRadius = 1.5f))
        // 10: Closes out the intro arc — smallest light of the first ten,
        // one hint. Wisps and gusts haven't shown up yet; that's what 11
        // onward is for.
        add(MazeLevel(category = "Darkness", levelNumber = 10, rows = 17, cols = 11, seed = 80010L, maxHints = 1, visionRadius = 1.4f))

        // Levels 11-49: procedurally continue the arc. Board size keeps
        // growing on the same kind of curve every other category uses
        // (capped at 26x15 to stay readable), and torch radius keeps
        // shrinking toward a floor instead of stopping dead at level 10's
        // value. Wisps appear from 11 (count grows every ten levels); gusts
        // join in from 26 once wisps are established.
        for (levelNumber in 11..49) {
            val rows = (17 + (levelNumber - 10)).coerceAtMost(26)
            val cols = (11 + (levelNumber - 10) * 4 / 10).coerceAtMost(15)
            val visionRadius = (1.4f - (levelNumber - 10) * 0.011f).coerceAtLeast(0.95f)
            val wispCount = when {
                levelNumber <= 15 -> 2
                levelNumber <= 25 -> 3
                levelNumber <= 35 -> 4
                else -> 5
            }
            val hasGusts = levelNumber >= 26

            add(
                MazeLevel(
                    category = "Darkness", levelNumber = levelNumber, rows = rows, cols = cols,
                    seed = 80000L + levelNumber, maxHints = 1, visionRadius = visionRadius,
                    wisps = scatteredWisps(rows, cols, wispCount, seed = 900000L + levelNumber),
                    hasGusts = hasGusts
                )
            )
        }

        // 50: Hand-tuned finale — biggest board, smallest torch in the set,
        // gusts snuffing the light every few seconds, and five wisps
        // scattered for anyone willing to detour for a bit more room to
        // breathe. Everything the category has taught, all at once.
        add(
            MazeLevel(
                category = "Darkness", levelNumber = DARKNESS_TOTAL_LEVELS, rows = 26, cols = 15,
                seed = 80000L + DARKNESS_TOTAL_LEVELS, maxHints = 1, visionRadius = 0.92f, hasGusts = true,
                wisps = scatteredWisps(26, 15, 5, seed = 900000L + DARKNESS_TOTAL_LEVELS)
            )
        )
    }

    fun isPlayable(category: String, levelNumber: Int): Boolean =
        find(category, levelNumber) != null
}
