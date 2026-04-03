# Zombie Lane Shooter

A fast-paced Android arcade game built with Kotlin and SurfaceView.

**Version:** 1.0.4 (`versionCode` 5) — see `app/build.gradle.kts`.

## Gameplay

- **Move** the ship left/right by dragging; movement stays inside a padded **safe area** (display cutouts respected).
- **Bullets fire automatically** on a timer. Fire rate combines the **equipped shooter** with **Fire Rate** upgrades (down to a minimum interval).
- **Destroy zombies** and earn **score** and **coins**. **Combo chains** multiply score; session coins are added to your wallet on **game over**.
- You **lose** when **health hits zero** (enemies that reach your row deal damage; bosses can hit you repeatedly at the bottom).
- **Chests** can drop after runs; **daily missions** and **login streaks** grant coins and chests. **Next-run buffs** (shield start, rapid-fire start) can come from chest rewards.

The sections below document stages, enemies, shop, power-ups, events, meta progression, UI, ads, audio, and code layout.

---

## Stages and progression

Defined in `StageManager` / `Stage.kt`:

| Stage | Name | Kills to boss* | Notes |
|------|------|----------------|--------|
| 1 | Outskirts | 10 | Normal only; no boss |
| 2 | Neon District | 15 | Normal + Zigzag; boss |
| 3 | Frozen Reach | 20 | + Fast |
| 4 | Dark Forest | 25 | + Splitter |
| 5 | Molten Core | 35 | Zigzag / Fast / Splitter |
| 6 | Nebula Gate | 45 | Harder mix |

\*With a boss, regular spawns stop after the kill quota; defeat the **boss** to advance (short transition banner).

**Endless mode:** After stage 6, procedural **Sector N** stages continue with scaling spawn rate, speed, kill requirements, and rotating **background** themes (Space, City, Lava, Forest, Ice).

---

## Enemies

**Types (`EnemyType`):**

- **NORMAL** — Straight fall; tiered by score (HP, speed, color, score/coins).
- **ZIGZAG** — Side-to-side sine motion; cyan; slightly slower speed modifier.
- **FAST** — Fast, low HP; orange. Splitter minions spawn as Fast.
- **SPLITTER** — On death, spawns two offset Fast enemies.
- **BOSS** — Large, slow, high HP, horns; centered spawn; in later stages can shoot; clamps at the bottom and can damage the player repeatedly.

**Scaling:** Score-based tiers, per-stage spawn/speed multipliers, and spawner interval that tightens with score (down to a floor). **Random events** can apply global speed/coin multipliers.

**Enemy fire:** After intro stages, **Zigzag** enemies may shoot on a long cooldown; **bosses** shoot in post-intro stages. **Shield** blocks one enemy bullet; otherwise you lose HP (with brief invincibility).

---

## Player health and damage

- **Max HP** starts at 3; **Health** upgrades add +1 per level.
- **Shield** (power-up): one hit buffer vs bullets or contact (then invincibility frames).
- **Near-death:** at 1 HP (when max HP > 1), a subtle red pulse overlay during play.

---

## Weapons (shooters)

Five shooters (`ShooterType` / `Shooter` in `ShooterType.kt`):

| Shooter | Role | Base fire (ms) | Damage × | Unlock (coins) |
|--------|------|----------------|----------|----------------|
| **BASIC** | Balanced | 130 | ×1.0 | Free |
| **DOUBLE** | Two lanes | 170 | ×0.8 | 50,000 |
| **SPREAD** | Center + angled | 250 | ×0.65 | 200,000 |
| **RAPID** | Very fast, small shots | 40 | ×0.3 | 100,000 |
| **LASER** | Fast tall shots | 20 | ×0.15 | 150,000 |

Damage per bullet: `(upgrade damage) × shooter multiplier`, minimum 1. Patterns in `BulletManager`.

**Shop:** Unlock with coins or **equip** if owned. **Rewarded ad** (when ready) grants **30 minutes** temporary access if you cannot afford unlock; when it expires, loadout falls back to BASIC if not owned.

---

## Upgrades (`UpgradeManager`)

Persistent coins and three lines (also on **game over**): **Damage** (+1/level), **Fire Rate** (−10 ms interval per level, floor cap), **Health** (+1 max HP/level). Cost: `20 + level × 15`. **High score** lives with the same prefs.

**Settings → Reset progress** clears coins, upgrades, high score, shooter unlock/equip prefs, **chest slots**, **login streak**, and **pending next-run buffs** (`RunBuffManager`). **Daily missions** use their own prefs and are **not** cleared by that reset.

---

## Daily missions (`DailyMissionManager`)

- **Three missions** per **24-hour** period; timer rolls a new set when the period ends.
- Objectives are rolled by kind: **kills**, **reach stage N**, or **use a shooter** (counts vary per mission).
- Complete objectives, then **Claim** on the Daily Missions screen for **coins** and sometimes a **chest** grant.
- **Milestones:** claiming 1 / 2 / all 3 missions in a period awards extra **coins**; the “all 3” milestone can add a **Rare** chest if a slot is free.

---

## Chests (`ChestManager`, `ChestType`)

- Up to **4 slots**. Chests have real-time **unlock timers** (Common shortest, Rare/Epic/Super longer — see `ChestType.unlockDurationMs`).
- **Sources:** run-end drops (rarity scales with stage/score; small chance for **Super**), daily mission claims/milestones, **login streak** milestones, and mission rewards.
- **Open** when ready: animated reveal; optional **rewarded** step to **double** chest payout vs claiming base rewards.
- Rewards can include **coins**, **temporary shooter** trials, **shield on next run**, and **rapid-fire duration at next run start** (`RunBuffManager` applies these in `resetGame()`).
- **Lucky roll** (~10%): **double values** or **rarity up** on open.
- **Merge:** two chests of the **same** type can combine into the next tier (Common→Rare→Epic→Super) via merge mode on the Chests screen.
- **Skip timer:** from the chest detail UI — **rewarded ad** when loaded; if the ad is not ready, the code may still clear the wait immediately (see `showRewardedSkipTimer` in `GameView`).

---

## Login streak (`StreakManager`)

- **Consecutive calendar-day** opens (first menu entry of the day).
- **Day 1:** Common chest; **day 3:** Rare; **day 7:** Epic — if a chest slot is available; otherwise a message explains slots are full.
- Missing a day resets the streak (milestone claim flags reset when the streak breaks).

---

## Power-ups

~**12%** drop on kill:

- **RAPID FIRE** — ~5 s of much faster firing.
- **SHIELD** — One-hit buffer.
- **BOMB** — Clears all enemies, awards score/coins, updates **stage/boss** state correctly.

---

## Combo (`ComboTracker`)

Kills within **1.5 s** chain the combo. Multiplier: `1 + combo × 0.2`. Floating text and colors scale with combo; **max combo** appears on game over.

---

## Random events (`GameEventManager`)

After ~**35 s**, events can fire every ~**22 s** cooldown:

- **ZOMBIE RUSH** — Faster enemies (5 s).
- **COIN RAIN** — Slower enemies, **×3** coins (8 s).
- **SWARM** — Burst of 8 enemies (skipped during active boss); screen shake.

---

## HUD and screens

**In-run HUD:** score, session coins (particles to icon), health, pause, shooter badge + temp-unlock timer, stage name/number and boss progress, combo, event banner, optional FPS (Settings).

**Menu** — Play, Shop, Settings; **chest strip** (timers / navigate to Chests), **Daily Missions** entry, **streak** line; high score and total coins; **Free reward** pill (rewarded ad when the unit is ready).

**Chests** — Slots, open/merge, skip timer (rewarded), reveal animation and optional double-value ad flow.

**Daily Missions** — Progress bars, claim buttons, time until period reset.

**Pause** — Resume, Settings, Quit; banner ads may show.

**Continue offer** — After death, **once per run**, a **rewarded** “continue” may appear before game over; declining or back finalizes game over.

**Game over** — stats, quick upgrades, Shop / Play again / Menu; **Double rewards** (rewarded) doubles **session coins** for that run when used.

**Shop** — unlock, equip, rewarded trial.

**Settings** — sound, music, vibration, Show FPS, reset (confirm tap), Back.

**System back:** play → pause; pause → resume; shop/settings → back; **chests** → menu (cancels in-progress reveal sensibly); **daily missions** → menu; **continue offer** → game over; game over → menu; menu → leave app.

---

## Visuals

Themed **backgrounds** (`BackgroundManager`), death **particles** (more for bosses), **screen shake**, **floating text**, ship **bitmaps** per shooter (`PlayerAssets`), enemy art (`EnemyAssets`), menu bitmaps (`MenuUiAssets`).

---

## Audio (`audio/`)

**Sound** effects (`SoundManager`), **music** (`MusicManager`), and **haptics** (`VibrationManager`) are coordinated through **`GameFeedback`** with **Settings** toggles for sound, music, and vibration.

---

## Advertising (`AdManager`)

**Banner** on menu / pause / game over (hidden in active play). **Interstitial** on a death cadence when loaded. **Rewarded** in Shop for temporary shooters, **continue** offers, chest timer skip, and other optional placements. Debug builds use **test unit IDs** from `local.properties` (`admob.*.id.test`) or Google sample IDs; **release** builds require production IDs via environment variables, `local.properties`, or Gradle properties (see `app/build.gradle.kts`).

---

## Engine

**Rendering:** `SurfaceView` + `Canvas`; **GameThread** ~60 FPS.

**States:** `MENU`, `PLAYING`, `PAUSED`, `CONTINUE_OFFER`, `GAME_OVER`, `SETTINGS`, `SHOP`, `CHESTS`, `DAILY_MISSIONS` (`GameState`).

---

## Architecture

```
com.zombielane.shooter/
├── GameActivity.kt
├── ads/AdManager.kt
├── audio/
│   ├── GameFeedback.kt, MusicManager.kt, SoundManager.kt, VibrationManager.kt
├── data/
│   ├── ShooterManager.kt, ShooterType.kt
│   ├── UpgradeManager.kt, SettingsManager.kt
│   ├── DailyMissionManager.kt
│   ├── ChestManager.kt, ChestType.kt, ChestVisualState.kt, ChestAnimState.kt, ChestRevealPhase.kt
│   ├── StreakManager.kt, RunBuffManager.kt
├── engine/
│   ├── GameView.kt, GameThread.kt, GameState.kt
│   ├── StageManager.kt, Stage.kt, BackgroundType.kt
│   ├── BackgroundManager.kt, ComboTracker.kt, GameEventManager.kt
├── objects/
│   ├── GameObject.kt, Player.kt, PlayerAssets.kt, EnemyAssets.kt
│   ├── Bullet.kt, BulletManager.kt, Enemy.kt, EnemySpawner.kt
│   ├── EnemyBullet.kt, EnemyBulletManager.kt
│   ├── PowerUp.kt, Particle.kt, CoinParticle.kt, FloatingText.kt
└── ui/
    ├── HUD.kt, MenuScreen.kt, MenuUiAssets.kt
    ├── PauseScreen.kt, GameOverScreen.kt, ContinueOfferScreen.kt
    ├── ShopScreen.kt, SettingsScreen.kt
    ├── ChestScreen.kt, ChestRevealUI.kt, ChestOpeningAnimator.kt, ChestRewardBackgroundSystem.kt
    └── DailyMissionsScreen.kt
```

Project **assets** (images, etc.) live under repo root `assets/` and are wired in via `app/build.gradle.kts` (`sourceSets`).

| Area | Main types |
|------|------------|
| Loop / rules | `GameView`, `GameThread`, `GameState` |
| Stages / BG | `StageManager`, `Stage`, `BackgroundManager` |
| Spawning | `EnemySpawner` |
| Entities | `Player`, `Enemy`, `Bullet`, `EnemyBullet`, `PowerUp`, particles, `FloatingText` |
| Combat helpers | `BulletManager`, `EnemyBulletManager`, `ComboTracker`, `GameEventManager` |
| Meta | `UpgradeManager`, `ShooterManager`, `SettingsManager`, `DailyMissionManager`, `ChestManager`, `StreakManager`, `RunBuffManager` |
| UI | `HUD`, menu / pause / game over / continue / shop / settings / chests / daily missions |
| Audio | `GameFeedback`, `SoundManager`, `MusicManager`, `VibrationManager` |

If gameplay code changes, update this README to match.

---

## Build & Run

**Prerequisites:** Android Studio (Ladybug+), JDK 17, Android SDK 35.

```bash
./gradlew assembleDebug
./gradlew installDebug   # connected device
```

**Play Store bundle:** `./gradlew bundleRelease` produces an `.aab`; the build renames/copy to `app/build/outputs/bundle/release/lane-shooter-<versionName>.aab`. Release builds require **production AdMob** IDs (see `app/build.gradle.kts`).

Or open the project in Android Studio and run.

---

## Tech stack

| Component | Choice |
|-----------|--------|
| Language | Kotlin |
| Rendering | SurfaceView + Canvas |
| Game loop | Dedicated thread @ ~60 FPS |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |
| Ads | Google Mobile Ads (Play services ads) |
| UI toolkit | Canvas-based custom screens (no Compose in gameplay) |
