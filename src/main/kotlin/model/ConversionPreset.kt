package model

enum class ConversionPreset(
    val category: ConversionCategory,
    val label: String,
    val description: String,
    val inputExtensions: List<String>,
    val outputExtension: String,
) {
    VIDEO_TO_MP3(
        category = ConversionCategory.VIDEO,
        label = "Video to MP3",
        description = "Extract audio from supported video files and save it as MP3.",
        inputExtensions = listOf("mp4", "mov", "mkv", "avi", "webm", "m4v", "mpeg", "mpg"),
        outputExtension = "mp3",
    ),
    VIDEO_TO_MP4(
        category = ConversionCategory.VIDEO,
        label = "Video to MP4",
        description = "Convert supported video files into MP4 video.",
        inputExtensions = listOf("mp4", "mov", "mkv", "avi", "webm", "m4v", "mpeg", "mpg"),
        outputExtension = "mp4",
    ),
    MP3_TO_MP4(
        category = ConversionCategory.SOUND,
        label = "MP3 to MP4",
        description = "Wrap MP3 audio into an MP4 video with a black background.",
        inputExtensions = listOf("mp3"),
        outputExtension = "mp4",
    ),
    IMAGE_TO_PNG(
        category = ConversionCategory.IMAGE,
        label = "Image to PNG",
        description = "Convert supported image files into PNG output.",
        inputExtensions = listOf(
            "png", "jpg", "jpeg", "webp", "bmp", "gif", "tif", "tiff",
            "avif", "heic", "heif", "ico"
        ),
        outputExtension = "png",
    ),
    IMAGE_TO_JPG(
        category = ConversionCategory.IMAGE,
        label = "Image to JPG",
        description = "Convert supported image files into JPG output.",
        inputExtensions = listOf(
            "png", "jpg", "jpeg", "webp", "bmp", "gif", "tif", "tiff",
            "avif", "heic", "heif", "ico"
        ),
        outputExtension = "jpg",
    ),
    IMAGE_TO_WEBP(
        category = ConversionCategory.IMAGE,
        label = "Image to WebP",
        description = "Convert supported image files into WebP output.",
        inputExtensions = listOf(
            "png", "jpg", "jpeg", "webp", "bmp", "gif", "tif", "tiff",
            "avif", "heic", "heif", "ico"
        ),
        outputExtension = "webp",
    );

    val inputExtensionSummary: String
        get() = inputExtensions.joinToString(", ") { ".$it" }

    val sourcePrompt: String
        get() = "Choose one or more supported files: $inputExtensionSummary."
}
