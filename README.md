# CID Quest

## Latest update — rebrand from Maze Quest, enemy mode removed entirely
- **Enemy mode is gone.** The whole "Enemies" category (its 50 levels), the
  patrol-guard obstacles that were mixed into later Ice Floor and Darkness
  levels, the patrol movement/collision/"caught" logic in
  `screens/MazeGameScreen.kt`, the threat-triggered heartbeat music, and the
  separate "Enemy Skins" shop shelf (`game/SkinsCatalog.kt`,
  `screens/ShopScreen.kt`) have all been removed. Every level keeps its
  original board size, seed, and hint budget — only the guard hazard was
  stripped out, so Ice Floor and Darkness play exactly as before minus the
  threat.
- **Rebranded Maze Quest → CID Quest**, with a fresh package id
  (`com.mentricstudios.cidquest`, replacing `com.mentricstudios.mazequest`)
  so this is a distinct app, not an update to the old listing. All in-app
  text (splash, loading screen, Terms, Settings support email, notification
  copy) now says CID Quest.
- **New logo & app icon** — an original magnifying-glass mark with the same
  maze-path idea from the old logo now living inside the lens, so it reads
  as "investigating a maze" rather than reusing the old squiggle-only mark.
  Same neon-teal/pink palette as before. See `res/drawable-nodpi/img_cid_logo.png`,
  `res/drawable/ic_cid_logo.xml`, `res/drawable/ic_launcher_foreground.xml`,
  and the `AnimatedMazeLogo()` / `AnimatedMazeIcon()` Canvas versions in
  `ComingSoonScreen.kt` / `screens/LoadingScreen.kt`.
- Note: the "CID" name is a common law-enforcement acronym as well as the
  title of a long-running TV show — this rebrand does not use that show's
  logo, characters, or any of its specific branding, only an original
  detective-themed mark, to stay clear of any trademark issue.

## Latest update — Mentric Studios splash screen + bug fixes
- **New: Studio intro splash.** App now opens with the "Mentric Studios"
  bumper video before the existing maze loading screen. The source clip's
  white background was keyed out and the logo composited onto this app's
  exact background gradient (`#070913 → #0F1428`) ahead of time, so it's a
  normal opaque `res/raw/studio_splash.mp4` played via Media3 ExoPlayer —
  no runtime alpha-video decoding, which keeps it reliable on every device.
  See `screens/StudioSplashScreen.kt` and the new `STUDIO_SPLASH` route
  wired in as the app's start destination in `MainActivity.kt`.
- **Bug fix — wrong support email.** Settings → Support/Contact Us sent mail
  to `mentrcstudios@gmail.com` (missing an "i") instead of
  `mentricstudios@gmail.com` — every support email was going to a dead
  address. Fixed in `screens/SettingsScreen.kt`.
- **Bug fix — Daily Reward dialog showed the wrong day once already
  claimed.** Reopening the Home screen's daily-reward popup after already
  claiming today re-derived the streak day with a function meant only for
  "what would claiming *right now* produce", which quietly falls back to
  "Day 1" once today's claim is already recorded — so a real streak (say,
  day 4) displayed as if only day 1 had ever been claimed. Fixed in
  `screens/HomeScreen.kt` to use the actual stored streak once already
  claimed.
- **Performance fix — maze generated twice per level load.** Opening any
  level ran the recursive-backtracker maze generator twice (once for the
  playable grid, once more inside the optimal-move-count calculation for
  star scoring) because the latter quietly rebuilt its own grid instead of
  reusing the one already built. Fixed in `game/MazeModels.kt` /
  `screens/MazeGameScreen.kt` — noticeable mainly on the bigger, late-game
  boards.

Native Android app built with **Kotlin + Jetpack Compose** (Canvas used for the
animated maze gameplay). Full maze gameplay is implemented — movement, hints,
win states — this is a real playable build, not just a placeholder.

## Latest update — Darkness grows to 50 levels, two new mechanics, star-count bug fixed
- **Darkness: 10 → 50 levels.** Levels 1-10 are untouched; 11-50 are new,
  following the same "grow the board, scale the guards" curve every other
  category already uses, plus two brand-new ideas layered on top once the
  fog itself is second nature — see `game/MazeLevels.kt` → `DARKNESS`.
- **New: Wisp pickups (from level 11).** Small glowing motes scattered
  through the fog — like the goal, they're never fully hidden, just dim
  until the torch gets close. Walking over one *permanently* widens the
  torch a notch for the rest of that attempt (capped, so it's a bonus, not
  a requirement), rewarding a deliberate detour off the shortest path. A
  new stat chip on the game screen tracks how many you've found. See
  `wispBonus` / `collectedWisps` in `screens/MazeGameScreen.kt`.
- **New: Gusts (from level 26).** Once wisps and guards are both
  established, the torch now periodically gutters down to a sliver for
  about a second before recovering — a "freeze and trust the memory trail"
  beat instead of only ever reacting to live light. See `gustMultiplier`
  in `screens/MazeGameScreen.kt`.
- **Bug fix — Darkness stars weren't counting.** `GameProgress.totalStars()`
  built its default level list from Classic + Enemies + Ice Floor only;
  Darkness was missing entirely, so clearing Darkness levels never moved
  the star count shown on Home/Categories or your purchasing power in the
  Shop. Fixed by including `MazeLevels.DARKNESS` in that list.
- **Bug fix — stale "0/10" label.** The Categories screen's default Darkness
  progress label and default parameter still said `0/10` from when the
  category only had 10 levels; now says `0/50` to match.

## Previous update — smarter hints, first-time tutorial, more animation polish
- **Hints reworked into "Guiding Light"** — the old hint just flashed a
  single next-step arrow for under two seconds, which was easy to miss and
  useless right at a junction. A hint now lights up a real stretch of the
  route (up to 4 steps ahead), with a little glowing spark that visibly
  travels the path before it fades — see `useHint()` and `drawHintTrail()`
  in `screens/MazeGameScreen.kt`.
- **New-player in-game guide** — the very first time anyone opens *any*
  level, a short 4-step "HOW TO PLAY" walkthrough appears (swipe to move,
  use hints, pause/restart, star scoring), fully skippable, and gameplay is
  paused underneath while it's up. Shown once per install
  (`util/OnboardingPrefs.kt` → `hasSeenGameTutorial`), independent of the
  Terms/Age onboarding. See `GameTutorialOverlay()` in `MazeGameScreen.kt`.
- **More entrance/feedback animation across screens** — level tiles on
  `screens/LevelSelectScreen.kt` now pop in staggered (spring scale + fade)
  instead of appearing flat, with a slow breathing pulse on locked tiles'
  lock icons; skin cards on `screens/ShopScreen.kt` pop in staggered the
  same way, plus a small celebratory scale/glow "pop" plays the instant a
  skin becomes equipped instead of the border color just silently changing.

## Previous update — new app icon & logo
- **New launcher icon** — replaced the old vector placeholder with your neon
  cyan-maze / pink-dot artwork, set up as a proper adaptive icon
  (`res/mipmap-anydpi-v26/ic_launcher.xml`) with correctly sized PNGs at every
  density (`mdpi` → `xxxhdpi`), plus round-icon variants for launchers that
  use circular masks.
- **In-app logo** — the same artwork (background keyed out to transparent,
  `res/drawable-nodpi/img_maze_logo.png`) now appears above the title on the
  Home screen with a gentle breathing/glow animation (`HomeLogoMark` in
  `HomeScreen.kt`), matching the app's existing neon-cyan/pink palette exactly
  (the artwork's colors already line up with `AccentTeal` / `AccentOrange`).
- Removed the old unused `ComingSoonScreen.kt` (dead code left over from an
  earlier placeholder build; it wasn't wired into any navigation route).

## What's new in this update
- **Play button is now perfectly centered** on the Home screen (not shifted
  by the top bar) and has a soft pulsing glow + press-bounce animation.
- **Button/card press animations** everywhere — Play button, gift icon,
  bottom icon row, category cards, and level tiles all scale down gently
  when tapped (`util/BounceModifier.kt`, reusable `Modifier.bounceClick()`).
- **Screen transitions** — fade + slide animations between every screen via
  `NavHost`'s `enterTransition` / `exitTransition`, instead of hard cuts.
- **Reworked Loading screen** — no more plain progress line. It now shows a
  Canvas-drawn maze icon (matching the app logo) with a glowing dot that
  travels along the maze path on a loop, a gradient progress bar, and
  rotating "Carving out corridors… / Hiding a few dead ends… / …" messages.
- **Terms & Age gate now only show once** — on first launch the user goes
  through Loading → Terms → Confirm Age → Home. After they confirm their
  age, a flag is saved locally (`util/OnboardingPrefs.kt`, backed by
  `SharedPreferences`), so on every future launch it skips straight from
  Loading → Home.

## What's inside
Full app flow, built with **Jetpack Compose Navigation**, all screens present
but the game itself is not playable yet — tapping any level shows a
"Coming Soon" dialog instead of real gameplay.

Flow (first launch): `Loading → Terms of Service → Confirm Age → Home (Play) → Categories → Level Select`
Flow (returning user): `Loading → Home (Play) → Categories → Level Select`

- `MainActivity.kt` — sets up the `NavHost` connecting every screen
- `navigation/Routes.kt` — route names
- `screens/LoadingScreen.kt` — splash with animated progress bar
- `screens/TermsScreen.kt` — Terms of Service & Privacy Policy consent screen
- `screens/AgeGateScreen.kt` — birthdate confirmation (month/day/year dropdown
  pickers)
- `screens/HomeScreen.kt` — logo, Play button, gift/star top bar, bottom icon
  row (sound, leaderboard, favorites, settings, shop — placeholders for now)
- `screens/CategoriesScreen.kt` — Classic / Ice Floor / Darkness /
  Traps / Lightning cards; only Classic is unlocked, rest show locked state
  plus a "Full game launching soon" banner
- `screens/LevelSelectScreen.kt` — 5-column level grid, level 1 unlocked
  (tapping it opens a "Coming Soon" dialog), rest show a lock icon
- `ui/theme/` — original color palette (deep indigo background, teal + orange
  accents, per-category colors), typography
- `res/drawable/ic_cid_logo.xml` & launcher icon — original vector maze
  artwork (not copied from any existing app)

Since nothing is playable yet, every interactive tap beyond level 1 either
does nothing (locked cards/tiles) or opens a small "Coming Soon" dialog — so
the whole flow can be demoed end-to-end today without pretending the maze
game itself exists yet.

## Building via GitHub Actions (no local Android Studio needed)
A ready workflow is included at `.github/workflows/android-build.yml`. Steps:

1. Push this whole folder to a new GitHub repo (root of the repo = this
   `CIDQuest` folder, i.e. `build.gradle.kts` and `settings.gradle.kts`
   should sit at the repo root).
2. Go to the repo's **Actions** tab — the workflow runs automatically on every
   push to `main`, or you can trigger it manually via **Run workflow**
   (`workflow_dispatch`).
3. Once it finishes (green check), open the run → **Artifacts** section at
   the bottom → download `cid-quest-debug-apk`. That zip contains
   `app-debug.apk` which you can install on any Android phone (`minSdk 24`,
   i.e. Android 7.0+) or drag into an emulator.
4. No local gradle wrapper jar is bundled in this zip (it's a binary file I
   can't generate offline) — the workflow generates it itself in CI via
   `gradle wrapper`, so you don't need to worry about it. If you later open
   the project in Android Studio locally, Studio will offer to generate the
   wrapper for you the first time you sync, or you can run
   `gradle wrapper --gradle-version 8.7` once you have Gradle installed
   locally.

## How to open & run (local Android Studio)
1. Install **Android Studio** (Koala/2024.1 or newer recommended).
2. `File → Open` → select the `CIDQuest` folder.
3. Let Gradle sync (needs internet the first time, to download Compose/AGP
   dependencies — I couldn't do this step myself since my sandbox has no
   network access, so please run it once in Studio).
4. Run on an emulator or device (`minSdk 24`).

## Easy things to customize
- **App name:** `app/src/main/res/values/strings.xml` → `app_name`
- **Colors:** `ui/theme/Color.kt`
- **Maze logo shape:** the `path` points inside `AnimatedMazeLogo()` in
  `ComingSoonScreen.kt`, or the static vector in
  `res/drawable/ic_cid_logo.xml`
- **Badge text / tagline:** directly inside `ComingSoonScreen.kt`

## Note on design
This was built as an **original app inspired by the classic-maze-game genre**
(lock-and-unlock levels, star currency, category themes like ice/darkness/
traps/lightning are common, non-exclusive game mechanics). To avoid any
IP/trademark issue, I did not copy the exact colors, logo, branding, or pixel
layout of the reference app you shared — everything here (palette, icon,
wording, maze pattern) is newly designed. When we build the real gameplay
later, I'd recommend we keep that same approach: same *genre and feel*, own
art and level design.
