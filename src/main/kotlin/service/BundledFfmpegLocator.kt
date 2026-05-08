package service

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

class BundledFfmpegLocator {
    private val resourcePath = "/bin/macos-aarch64/ffmpeg"
    private val cacheDirectory: Path = Path.of(
        System.getProperty("user.home"),
        "Library",
        "Caches",
        "ConvertingMedia",
        "bin",
    )
    private val extractedBinary: Path = cacheDirectory.resolve("ffmpeg")

    fun resolveExecutable(): Path {
        if (extractedBinary.exists() && extractedBinary.isRegularFile()) {
            ensureExecutable(extractedBinary)
            return extractedBinary
        }

        Files.createDirectories(cacheDirectory)

        val resourceStream = javaClass.getResourceAsStream(resourcePath)
            ?: throw ConversionException.MissingBundledBinary(resourcePath)

        resourceStream.use { input ->
            Files.newOutputStream(extractedBinary).use { output ->
                input.copyTo(output)
            }
        }

        ensureExecutable(extractedBinary)
        return extractedBinary
    }

    private fun ensureExecutable(path: Path) {
        try {
            val permissions = mutableSetOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE,
            )
            Files.setPosixFilePermissions(path, permissions)
        } catch (error: UnsupportedOperationException) {
            if (!path.toFile().setExecutable(true, false)) {
                throw ConversionException.ExtractionFailed("Could not mark bundled ffmpeg as executable.")
            }
        } catch (error: IOException) {
            throw ConversionException.ExtractionFailed("Could not prepare bundled ffmpeg: ${error.message}")
        }
    }
}
