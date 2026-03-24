# Zombie Lane Shooter

A fast-paced Android arcade game built with Kotlin and SurfaceView.

## Gameplay

- **Move** your ship left/right by touching and dragging
- **Bullets fire automatically** toward the top of the screen
- **Destroy zombies** before they reach the bottom
- Earn **score** and **coins** for each kill
- Game ends when a zombie passes your ship — tap to restart

## Architecture

```
com.zombielane.shooter/
├── GameActivity.kt          # Single fullscreen activity
├── engine/
│   ├── GameThread.kt        # Game loop targeting 60 FPS
│   └── GameView.kt          # SurfaceView: input, update, draw
├── objects/
│   ├── GameObject.kt        # Abstract base (position, bounds, collision)
│   ├── Player.kt            # Player ship with touch-follow movement
│   ├── Bullet.kt            # Auto-fired projectiles
│   ├── Enemy.kt             # Zombie enemies (3 tiers by difficulty)
│   ├── EnemySpawner.kt      # Wave/spawn logic with scaling difficulty
│   └── Particle.kt          # Death explosion particles
└── ui/
    └── HUD.kt               # Score, coins, game-over overlay
```

## Build & Run

**Prerequisites:** Android Studio (Ladybug+), JDK 17, Android SDK 35.

```bash
# Command line
./gradlew assembleDebug

# Install to connected device
./gradlew installDebug
```

Or open the project in Android Studio and hit **Run**.

## Tech Stack

| Component     | Choice              |
|---------------|---------------------|
| Language      | Kotlin              |
| Rendering     | SurfaceView + Canvas |
| Game Loop     | Dedicated thread @ 60 FPS |
| Min SDK       | 24 (Android 7.0)    |
| Target SDK    | 35                  |
