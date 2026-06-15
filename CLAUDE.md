# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

**Tertiary Fractal (Android)** — a native Android fractal-art generator (Kotlin / Jetpack
Compose / Material 3, MVVM, fully offline). It is the Android port of the iOS app of the same
name (`../../iOS/fractalapp`). Type a *seed* (any word/number) or tune parameters to generate
colorful Mandelbrot, Julia, Burning Ship, Tricorn, Phoenix and Celtic fractals, rendered live
on the GPU with **OpenGL ES 2.0**. Pinch/double-tap to zoom, drag to pan, save HD art to the
gallery or share. No backend; everything runs on-device.

- Application ID: `com.tertiaryinfotech.fractalapp` · Play listing name: **Tertiary Fractal**
- minSdk 24 · targetSdk/compileSdk 36 · JDK 17 · Gradle 8.14.3 · AGP 8.13.0 · Kotlin 2.0.21

> Keep the Android and iOS apps separate — do not share build files or mix the two projects.

## Build & run

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug          # debug APK
./gradlew :app:bundleRelease          # signed release AAB for Play (needs keystore.properties)
./gradlew installDebug                 # install on the connected device/emulator
```

## Architecture (MVVM)

```
model/   FractalType, ColorPalette, FractalParameters, FractalPreset (ports of the iOS models)
seed/    SeedGenerator — FNV-1a + splitmix64 → deterministic params from a text seed
gl/      FractalShaders (GLSL ES 2.0 escape-time shader), FractalRenderer (GLSurfaceView.Renderer
         + off-screen render-to-Bitmap export), FractalGLSurfaceView (pinch/pan/double-tap)
vm/      FractalViewModel — holds parameters + UI state; screen wires captureProvider for export
ui/      HomeScreen (full-bleed GL view + seed bar + actions), ControlsSheet, GallerySheet,
         AboutSheet, Theme
util/    PhotoSaver (MediaStore / legacy), Haptics
MainActivity.kt  — edge-to-edge Compose host
```

### Conventions & gotchas
- **GLSL ES 2.0 has no `switch`** and needs a constant loop bound — `FractalShaders.FRAGMENT`
  uses if/else for families/palettes and loops to 700 with an early `break` at `uMaxIter`.
- **`FractalType.value` / `ColorPalette.value` are passed to the shader** — keep enum order in
  sync with the if/else branches in the shader.
- **`adb screencap` does not capture the GL SurfaceView on the emulator with `-gpu host`**
  (blank white frame). Capture on a physical device, or relaunch the emulator with
  `-gpu swiftshader_indirect` (software GL is slow → can ANR). Phone caps are 720×1600/1080×2400;
  Play needs ≤2:1 ratio, so pad to **1080×1920** (`sips --resampleHeight 1920` then
  `--padToHeightWidth 1920 1080 --padColor 14101F`).
- **Screenshot/demo hook**: `adb shell "am start -n …/.MainActivity --es demo 'Seahorse Valley'"`
  opens directly on a named preset. Quote the whole `am` command for the *device* shell so the
  preset name's space survives.
- Saving uses MediaStore on API 29+ (no permission); API ≤28 requests `WRITE_EXTERNAL_STORAGE`.
- No data collected, no network, no tracking → Data safety = **No data collected**.

## Release / Play Store

- Signing: gitignored `keystore.properties`; upload keystore at
  `~/.android-keystores/tertiary-fractal-upload.jks` (alias `tertiary-fractal`). **Back it up.**
- `./gradlew :app:bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`.
- Submission flow + gotchas: `.claude/skills/play-store-submission/`. Store copy + assets in
  `store-assets/` (icon 512, feature 1024×500, screenshots 1080×1920).
- Advertising-ID declaration: **No** (the app pulls in no ads/analytics SDKs).

## Do not commit
`keystore.properties`, `*.jks`/`*.keystore`, `local.properties`, `/build`, `/.gradle`, `/.idea`.
