package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun classpathPainterResource(resourcePath: String): Painter {
    return remember(resourcePath) {
        val resourceBytes = ResourceLoader.javaClass.classLoader
            .getResourceAsStream(resourcePath)
            ?.readAllBytes()
            ?: error("Missing resource: $resourcePath")

        BitmapPainter(resourceBytes.decodeToImageBitmap())
    }
}

private object ResourceLoader
