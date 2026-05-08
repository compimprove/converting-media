package service

import model.ConversionPreset
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

class ConversionService(
    private val ffmpegLocator: BundledFfmpegLocator = BundledFfmpegLocator(),
) {
    private val unsupportedPresetMessages by lazy {
        buildMap {
            if (!hasEncoder("webp") && !hasEncoder("libwebp")) {
                put(
                    ConversionPreset.IMAGE_TO_WEBP,
                    "This bundled ffmpeg build does not include a WebP encoder, so WebP output is not available.",
                )
            }
        }
    }

    fun convert(inputs: List<Path>, outputDir: Path, preset: ConversionPreset): List<Path> {
        unsupportedPresetMessages[preset]?.let { message ->
            throw ConversionException.UnsupportedPreset(message)
        }

        val ffmpegExecutable = ffmpegLocator.resolveExecutable()
        val outputPaths = inputs.map { input ->
            outputDir.resolve("${input.nameWithoutExtension}.${preset.outputExtension}")
        }

        inputs.zip(outputPaths).forEach { (input, output) ->
            runFfmpeg(ffmpegExecutable, input, output, preset)
        }

        return outputPaths
    }

    fun isPresetSupported(preset: ConversionPreset): Boolean = preset !in unsupportedPresetMessages

    private fun runFfmpeg(ffmpegExecutable: Path, input: Path, output: Path, preset: ConversionPreset) {
        val command = mutableListOf(ffmpegExecutable.toString())
        command += argumentsFor(input, output, preset)

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val processOutput = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            val message = if (processOutput.isNotBlank()) processOutput else "ffmpeg exited with status $exitCode."
            throw ConversionException.ProcessFailed(message)
        }
    }

    private fun argumentsFor(input: Path, output: Path, preset: ConversionPreset): List<String> {
        return when (preset) {
            ConversionPreset.VIDEO_TO_MP3 -> listOf(
                "-y",
                "-i", input.toString(),
                "-vn",
                "-codec:a", "libmp3lame",
                "-q:a", "2",
                output.toString(),
            )

            ConversionPreset.MP3_TO_MP4 -> listOf(
                "-y",
                "-f", "lavfi",
                "-i", "color=c=black:s=1280x720:r=1",
                "-i", input.toString(),
                "-shortest",
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-tune", "stillimage",
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-b:a", "192k",
                output.toString(),
            )

            ConversionPreset.VIDEO_TO_MP4 -> listOf(
                "-y",
                "-i", input.toString(),
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-b:a", "192k",
                "-movflags", "+faststart",
                output.toString(),
            )

            ConversionPreset.IMAGE_TO_PNG -> listOf(
                "-y",
                "-i", input.toString(),
                "-frames:v", "1",
                "-c:v", "png",
                output.toString(),
            )

            ConversionPreset.IMAGE_TO_JPG -> listOf(
                "-y",
                "-i", input.toString(),
                "-frames:v", "1",
                "-q:v", "2",
                "-pix_fmt", "yuvj420p",
                output.toString(),
            )

            ConversionPreset.IMAGE_TO_WEBP -> listOf(
                "-y",
                "-i", input.toString(),
                "-frames:v", "1",
                "-c:v", "webp",
                "-quality", "90",
                "-compression_level", "4",
                output.toString(),
            )
        }
    }

    private fun hasEncoder(encoderName: String): Boolean {
        val ffmpegExecutable = ffmpegLocator.resolveExecutable()
        val process = ProcessBuilder(
            ffmpegExecutable.toString(),
            "-hide_banner",
            "-encoders",
        )
            .redirectErrorStream(true)
            .start()

        val processOutput = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        if (exitCode != 0) return false

        return processOutput.lineSequence().any { line ->
            line.trim().split(Regex("\\s+")).drop(1).firstOrNull() == encoderName
        }
    }
}
