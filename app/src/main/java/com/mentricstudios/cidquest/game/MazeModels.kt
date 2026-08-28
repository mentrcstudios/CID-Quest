package com.mentricstudios.cidquest.game

import kotlin.random.Random

/**
 * Core maze data structures shared by every game mode.
 *
 * A maze starts as a perfect grid maze (spanning tree — exactly one route
 * between any two cells) generated with a seeded recursive-backtracker,
 * then gets a "braiding" pass that opens some extra walls to add real
 * loops — see [MazeGenerator.generate]. Both passes are deterministic given
 * the same [seed], so a level always reproduces the exact same layout.
 */
enum class Direction(val rowDelta: Int, val colDelta: Int) {
    NORTH(-1, 0),
    EAST(0, 1),
    SOUTH(1, 0),
    WEST(0, -1);

    fun opposite(): Direction = when (this) {
        NORTH -> SOUTH
        EAST -> WEST
        SOUTH -> NORTH
        WEST -> EAST
    }
}

data class CellPos(val row: Int, val col: Int) {
    fun step(dir: Direction) = CellPos(row + dir.rowDelta, col + dir.colDelta)
}

/**
 * Same `cell.open[dir]` / `cell.open[dir] = true` interface the rest of the
 * code already uses, but backed by a plain 4-slot array instead of a
 * `HashMap<Direction, Boolean>`. The maze board redraws every animation
 * frame and checks all 4 sides of every visible cell to decide which walls
 * to draw, so this was getting hashed + boxed/unboxed thousands of times a
 * second on bigger boards — swapping the storage keeps every call site
 * identical while cutting that out entirely.
 */
class DirectionFlags {
    private val flags = BooleanArray(4)
    operator fun get(dir: Direction): Boolean = flags[dir.ordinal]
    operator fun set(dir: Direction, value: Boolean) { flags[dir.ordinal] = value }
}

/** Which sides of this cell are open passages (true = you can walk through). */
class Cell {
    val open = DirectionFlags()
}

class MazeGrid(val rows: Int, val cols: Int) {
    private val cells = Array(rows) { r -> Array(cols) { c -> Cell() } }

    operator fun get(pos: CellPos): Cell = cells[pos.row][pos.col]

    /** Direct row/col access for hot paths (e.g. the per-frame board render)
     *  that would otherwise have to allocate a throwaway [CellPos] just to
     *  call [get]. */
    fun cellAt(row: Int, col: Int): Cell = cells[row][col]

    fun inBounds(pos: CellPos) = pos.row in 0 until rows && pos.col in 0 until cols

    /** Is there an open passage from [pos] in [dir]? */
    fun canMove(pos: CellPos, dir: Direction): Boolean {
        val target = pos.step(dir)
        if (!inBounds(target)) return false
        return this[pos].open[dir] == true
    }

    fun openWall(a: CellPos, dir: Direction) {
        this[a].open[dir] = true
        val b = a.step(dir)
        if (inBounds(b)) this[b].open[dir.opposite()] = true
    }
}

object MazeGenerator {

    /**
     * Recursive-backtracker perfect-maze generation, deterministic given
     * [seed], followed by a "braiding" pass that opens extra walls to turn
     * that single-path tree into a maze with real loops — and then a
     * second pass that *guarantees* no cell is left a true dead end (see
     * [eliminateDeadEnds]).
     *
     * A pure perfect maze has exactly one route between any two cells —
     * which means once a chasing guard is between you and anywhere else,
     * there is *no* way around it, only backward past it. [braidChance] is
     * the probability, checked once per internal wall, of opening it too
     * (creating a loop) even though the perfect-maze pass already connected
     * that pair of cells some other way — but a random-chance pass alone
     * can still miss an entire corridor (every wall along it independently
     * failing its roll), leaving a genuine single-entrance dead end
     * untouched. The follow-up pass doesn't leave that to chance: every
     * cell with only one exit gets a second one forced open, so a guard
     * blocking a dead end's only opening is never actually a trap.
     */
    fun generate(rows: Int, cols: Int, seed: Long, braidChance: Double = 0.16): MazeGrid {
        val grid = generatePerfectMaze(rows, cols, seed)
        braid(grid, rows, cols, seed, braidChance)
        return grid
    }

    private fun generatePerfectMaze(rows: Int, cols: Int, seed: Long): MazeGrid {
        val grid = MazeGrid(rows, cols)
        val rnd = Random(seed)
        val visited = Array(rows) { BooleanArray(cols) }
        val stack = ArrayDeque<CellPos>()

        val start = CellPos(0, 0)
        visited[0][0] = true
        stack.addLast(start)

        while (stack.isNotEmpty()) {
            val current = stack.last()
            val unvisitedNeighbors = Direction.values().filter { dir ->
                val next = current.step(dir)
                grid.inBounds(next) && !visited[next.row][next.col]
            }

            if (unvisitedNeighbors.isEmpty()) {
                stack.removeLast()
                continue
            }

            val dir = unvisitedNeighbors[rnd.nextInt(unvisitedNeighbors.size)]
            val next = current.step(dir)
            grid.openWall(current, dir)
            visited[next.row][next.col] = true
            stack.addLast(next)
        }

        return grid
    }

    /**
     * Walks every internal wall exactly once (checking only EAST/SOUTH from
     * each cell, so each wall between two cells is only ever considered a
     * single time) and opens a [braidChance] fraction of the ones that
     * aren't already open, creating loops in what was a pure spanning tree.
     * Uses its own seeded [Random] — derived from but distinct from the
     * generation seed — so the same level always braids the exact same way.
     */
    private fun braid(grid: MazeGrid, rows: Int, cols: Int, seed: Long, braidChance: Double) {
        if (braidChance <= 0.0) return
        val rnd = Random(seed xor 0x5DEECE66DL)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val pos = CellPos(r, c)
                for (dir in BRAID_DIRECTIONS) {
                    val next = pos.step(dir)
                    if (!grid.inBounds(next)) continue
                    if (grid.canMove(pos, dir)) continue
                    if (rnd.nextDouble() < braidChance) {
                        grid.openWall(pos, dir)
                    }
                }
            }
        }
        eliminateDeadEnds(grid, rows, cols, rnd)
    }

    /**
     * The probabilistic pass above can still miss a stretch of maze
     * entirely — every wall along it is an independent coin flip, so a
     * whole corridor can fail every single roll and stay a true one-way-in
     * dead end. That's the actual bug behind "only one path to escape":
     * a guard blocking a dead end's single opening leaves genuinely no way
     * out, no matter how many loops exist elsewhere in the level.
     *
     * This pass doesn't leave it to chance. It walks every cell and, for
     * any with only one open side, forces a second one open — so no cell
     * in any level is ever a true dead end, guaranteed rather than likely.
     */
    private fun eliminateDeadEnds(grid: MazeGrid, rows: Int, cols: Int, rnd: Random) {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val pos = CellPos(r, c)
                val openCount = Direction.values().count { grid.canMove(pos, it) }
                if (openCount > 1) continue
                val closedDirs = Direction.values().filter { dir ->
                    val next = pos.step(dir)
                    grid.inBounds(next) && !grid.canMove(pos, dir)
                }
                if (closedDirs.isNotEmpty()) {
                    grid.openWall(pos, closedDirs[rnd.nextInt(closedDirs.size)])
                }
            }
        }
    }

    // EAST/SOUTH only — walking every cell and only checking these two
    // directions still reaches every internal wall in the grid exactly
    // once (the WEST wall of one cell is the EAST wall of its neighbor).
    private val BRAID_DIRECTIONS = listOf(Direction.EAST, Direction.SOUTH)

    /**
     * Breadth-first distance (in cell steps) from [start] to every reachable cell.
     * Used to stagger the maze reveal animation as an outward "ink" wave from the
     * entrance, instead of the whole grid popping in at once.
     */
    fun distancesFrom(grid: MazeGrid, start: CellPos): Array<IntArray> {
        val dist = Array(grid.rows) { IntArray(grid.cols) { -1 } }
        val queue = ArrayDeque<CellPos>()
        dist[start.row][start.col] = 0
        queue.addLast(start)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val d = dist[current.row][current.col]
            for (dir in Direction.values()) {
                if (!grid.canMove(current, dir)) continue
                val next = current.step(dir)
                if (dist[next.row][next.col] == -1) {
                    dist[next.row][next.col] = d + 1
                    queue.addLast(next)
                }
            }
        }
        return dist
    }

    /** Breadth-first shortest path between two cells, used for hints & star scoring. */
    fun shortestPath(grid: MazeGrid, start: CellPos, goal: CellPos): List<CellPos> {
        if (start == goal) return listOf(start)
        val cameFrom = HashMap<CellPos, CellPos>()
        val visited = HashSet<CellPos>()
        val queue = ArrayDeque<CellPos>()
        queue.addLast(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == goal) break
            for (dir in Direction.values()) {
                if (!grid.canMove(current, dir)) continue
                val next = current.step(dir)
                if (next !in visited) {
                    visited.add(next)
                    cameFrom[next] = current
                    queue.addLast(next)
                }
            }
        }

        if (goal !in visited) return emptyList()

        val path = ArrayDeque<CellPos>()
        var step = goal
        path.addFirst(step)
        while (step != start) {
            step = cameFrom[step] ?: return emptyList()
            path.addFirst(step)
        }
        return path.toList()
    }
}

/**
 * One patrolling enemy for the "Enemies" category. [from]/[to] are just two
 * cells in the level's grid — the actual walkable route between them is
 * derived at runtime via [MazeGenerator.shortestPath] (BFS, so it's still
 * correct even with the braided maze's loops), and the enemy simply
 * ping-pongs back and forth along whatever route that finds forever.
 */
data class EnemyPatrol(
    val from: CellPos,
    val to: CellPos,
    /** Milliseconds to glide one grid cell — lower is faster/deadlier. */
    val stepMillis: Int = 420
)

/** Static definition of one playable level. */
data class MazeLevel(
    val category: String,
    val levelNumber: Int,
    val rows: Int,
    val cols: Int,
    val seed: Long,
    val maxHints: Int = 2,
    val enemies: List<EnemyPatrol> = emptyList(),
    /** Extra-connection density for [MazeGenerator.generate]'s braiding pass — higher means more alternate routes/escape options. */
    val braidChance: Double = 0.16
) {
    val start = CellPos(0, 0)
    val goal = CellPos(rows - 1, cols - 1)

    fun buildGrid(): MazeGrid = MazeGenerator.generate(rows, cols, seed, braidChance)

    /**
     * Shortest-path move count for star scoring. Takes the already-built
     * [grid] instead of regenerating one internally — every call site already
     * has one via [buildGrid], and generating a second, identical grid every
     * time a level loaded was pure wasted work (worse the bigger the board).
     */
    fun optimalMoveCount(grid: MazeGrid): Int {
        val path = MazeGenerator.shortestPath(grid, start, goal)
        return (path.size - 1).coerceAtLeast(1)
    }
}
