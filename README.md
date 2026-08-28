# Cid Quest

Native Android app built with **Kotlin + Jetpack Compose** (Canvas used for the
animated maze gameplay). A real, playable maze-chase game — movement, hints,
patrol guards, win/caught states are all implemented, not a placeholder.

## What this is
Cid Quest is a maze-escape game: find your way from the entrance to the exit
while dodging patrol guards — get too close and a guard breaks off its
fixed route to actively chase you down (live-pathfinding toward your current
cell every step); stay clear of it for a full 5 seconds and it gives up and
heads back to its route. Getting caught resets the level. 50 hand-scaled
levels, growing from a small, guard-light intro board up to a large,
six-guard finale.

Flow (first launch): `Loading → Confirm Age (18+) → Home (Play → straight into a level)`
Flow (returning user): `Loading → Home (Play → straight into wherever you left off)`

- `MainActivity.kt` — sets up the `NavHost` connecting every screen
- `navigation/Routes.kt` — route names
- `screens/LoadingScreen.kt` — flat splash with a plain progress bar
- `screens/AgeGateScreen.kt` — simple "Are you 18+?" Yes/No confirmation
- `screens/HomeScreen.kt` — logo, Play button, sound toggle, and the
  character-photo picker (gallery picker lives right here — no separate
  screen for it). Play always drops you straight into the first level you
  haven't cleared yet; there's no level-select grid anymore.
- `screens/MazeGameScreen.kt` — the actual maze gameplay: movement, patrol
  guards (rendered as your own/gallery-picked photo vs. three fixed guard
  photos) that break off their patrol route to actively chase you once
  you're spotted and give up after 5 seconds clear, hints ("Guiding
  Light"), pause menu (with vibration/D-pad toggles), win/caught states —
  no scoring, a level is simply cleared or not
- `ui/theme/` — flat Navy & Gold palette, no gradients/glow effects
- `res/mipmap-*` / `res/drawable-nodpi/img_cid_logo.png` — launcher icon +
  in-app logo mark
- `res/drawable/enemy_1.png` / `enemy_2.png` / `enemy_3.png` / `player_default.png`
  — the character photos; `util/CharacterPhoto.kt` decodes, center-crops,
  and circle-masks them once (cached for the process) and manages the
  player's optional custom gallery photo

This is a private/personal build — no stars, coins, shop, or daily-reward
economy, no Privacy Policy screen, and the character art is real photos
instead of the original illustrated spark/ghost sprites.

## Levels
`game/MazeLevels.kt` holds the full 50-level catalog. Levels 1-2 are
hand-tuned intros; 3-50 are procedurally scaled — board size grows, guard
count climbs from 2 up to 6, and guards get faster as the level number rises
(capped so it never becomes unfair).

## Easy things to customize
- **App name:** `app/src/main/res/values/strings.xml` → `app_name`
- **Colors:** `ui/theme/Color.kt`
- **Type scale:** `ui/theme/Type.kt`
- **Levels:** `game/MazeLevels.kt`

## Building via GitHub Actions (no local Android Studio needed)
A ready workflow is included at `.github/workflows/android-build.yml`. Steps:

1. Push this whole folder to a new GitHub repo (root of the repo = this
   folder, i.e. `build.gradle.kts` and `settings.gradle.kts` should sit at
   the repo root).
2. Go to the repo's **Actions** tab — the workflow runs automatically on every
   push to `main`, or you can trigger it manually via **Run workflow**
   (`workflow_dispatch`).
3. Once it finishes (green check), open the run → **Artifacts** section at
   the bottom. Two APKs are built:
   - `cid-quest-debug-apk` → `app-debug.apk` — unshrunk, larger, matches
     exactly what you've been testing so far.
   - `cid-quest-release-apk` → `app-release.apk` — code/resource-shrunk via
     R8, meaningfully smaller. Signed with the debug key (this isn't a Play
     Store release, just a smaller build) so it installs the exact same way
     as the debug one — no separate signing setup needed.
   Either one installs on any Android phone (`minSdk 24`, i.e. Android
   7.0+) or drags into an emulator.
4. No local gradle wrapper jar is bundled in this zip (it's a binary file
   that can't be generated offline) — the workflow generates it itself in CI
   via `gradle wrapper`, so you don't need to worry about it. If you later
   open the project in Android Studio locally, Studio will offer to generate
   the wrapper for you the first time you sync, or you can run
   `gradle wrapper --gradle-version 8.7` once you have Gradle installed
   locally.

## How to open & run (local Android Studio)
1. Install **Android Studio** (Koala/2024.1 or newer recommended).
2. `File → Open` → select this folder.
3. Let Gradle sync (needs internet the first time, to download Compose/AGP
   dependencies).
4. Run on an emulator or device (`minSdk 24`).

## Before publishing
- **App ID:** currently `com.mentricstudios.cidquest` (set in
  `app/build.gradle.kts`) — pick your own if this is already taken on the
  store you're publishing to.
- **AdMob App ID:** `AndroidManifest.xml` currently ships Google's public
  *test* App ID so the project builds and shows test ads out of the box.
  Swap it for your real AdMob App ID (and the real ad unit IDs in
  `ads/AdIds.kt`) before release, or ads will stay in test mode.
- **Store review:** an earlier build of this codebase (as a different,
  multi-mode game) was rejected once for looking like an unmodified
  template and once for a blocked package name. This build is a real,
  substantially different game (single focused mode, new branding, new
  package name) — worth still double-checking your store's policies on
  originality before submitting.
