# Korean Android fork

This branch extends wonst719's Korean Exult 1.12.x work for Android. It keeps
the existing PC-compatible CP949 font and script path, then adds the Android
bridges needed to run that path safely.

## Included changes

- CP949-safe Android console and character-name JNI bridges.
- Korean character names, limited to the game's 16-byte actor record.
- Android internal-storage path fixes and ZIP support through the NDK zlib.
- Optional locally supplied game and Korean-mod archives.
- Physical controller support with a right-stick mouse cursor, click/drag,
  double-click, shortcuts, and bounded diagnostic logs.

See [KOREAN_ANDROID_CONTROLS.md](KOREAN_ANDROID_CONTROLS.md) for the controller
mapping and diagnostics format.

## Content and copyright boundary

This repository contains engine source only. It does **not** contain Ultima VII
game data, Korean translation data, Korean font files, or a prebuilt APK that
embeds any of those files. You must own the games and supply every content file
locally.

An ordinary Android build works without private assets and retains Exult's
launcher import flow. A private build may optionally pass
`KOREAN_ASSET_ROOT=/absolute/path/to/asset-root` as a Gradle project property.
Only files below that local root are added to the APK; they remain untracked and
must not be committed unless you have redistribution rights.

When present, the optional asset root has this layout:

```text
asset-root/
  patch/                 # Common Korean font/message files
  bundled-content/
    blackgate-static-local.zip
    serpentisle-static-local.zip
    koreanbg.zip
    koreansi.zip
```

Any subset is accepted. Missing bundled archives simply leave the standard
launcher import controls available.

## Building

Follow [README.md](README.md) for the normal out-of-tree Android build. To add
private local assets, append the property to the Android make invocation:

```sh
make -C android -j4 \
  GRADLE_PROJECT_PROPS="--project-prop KOREAN_ASSET_ROOT=/absolute/path/to/asset-root"
```

The Android package ID is `info.exult.korean`, so it can coexist with the
official `info.exult` package. Debug APKs use the local Android debug key;
release builds require your own signing key.

## Verification status

The Korean name bridge, legacy console bridge, controller state machine, log
rotation, APK signature/container structure, and bundled font coverage were
tested during development. Black Gate Korean gameplay and Korean character-name
entry were confirmed on the target Android device. The controller mapping was
packaged and statically verified; device-specific sensitivity and button
mapping should be checked from the bounded diagnostic logs on each controller.

This is an independent fork and is not an official Exult release.
