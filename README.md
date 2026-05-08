# Converting Media

A macOS-first Kotlin Compose Desktop app for:

- converting common video formats such as `.mp4`, `.mov`, `.mkv`, `.avi`, `.webm`, `.m4v`, `.mpeg`, and `.mpg` to `.mp3`
- converting common video formats such as `.mp4`, `.mov`, `.mkv`, `.avi`, `.webm`, `.m4v`, `.mpeg`, and `.mpg` to `.mp4`
- converting `.mp3` to `.mp4`
- reserving an image conversion section for a later update

The app bundles its own Apple Silicon `ffmpeg` binary, so the host machine does not need `ffmpeg` installed separately.

## Run In Development

```bash
cd /Users/compi/source/personal/converting-media
./gradlew run
```

## Build A DMG

```bash
cd /Users/compi/source/personal/converting-media
./gradlew packageDmg
```

## Notes

- The bundled binary is stored at `src/main/resources/bin/macos-aarch64/ffmpeg`
- The current bundled binary targets Apple Silicon macOS
- The app groups conversions into video, sound, and image sections
- The app chooses files by preset, supports batch conversion, and opens the output folder after conversion
