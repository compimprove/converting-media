package model

enum class ConversionCategory(
    val label: String,
    val description: String,
) {
    VIDEO(
        label = "Video Converting",
        description = "Convert supported video files into audio or MP4 output.",
    ),
    SOUND(
        label = "Sound Converting",
        description = "Convert supported audio files into video output.",
    ),
    IMAGE(
        label = "Image Converting",
        description = "Convert supported image files into PNG, JPG, or WebP output.",
    ),
}
