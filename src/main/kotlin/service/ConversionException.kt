package service

sealed class ConversionException(message: String) : Exception(message) {
    class MissingBundledBinary(resourcePath: String) :
        ConversionException("Bundled ffmpeg was not found at $resourcePath.")

    class ExtractionFailed(message: String) : ConversionException(message)

    class UnsupportedPreset(message: String) : ConversionException(message)

    class ProcessFailed(message: String) : ConversionException(message)
}
