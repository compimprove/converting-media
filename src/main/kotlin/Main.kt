import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.LaunchedEffect
import java.awt.Color
import java.awt.Toolkit
import ui.classpathPainterResource
import ui.ConverterApp

fun main() {
    System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua")

    application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "",
        icon = classpathPainterResource("app-icon.png"),
    ) {
        LaunchedEffect(Unit) {
            val screenBounds = window.graphicsConfiguration.bounds
            val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.graphicsConfiguration)

            window.background = Color(0x0D, 0x16, 0x25)

            window.setBounds(
                screenBounds.x + screenInsets.left,
                screenBounds.y + screenInsets.top,
                screenBounds.width - screenInsets.left - screenInsets.right,
                screenBounds.height - screenInsets.top - screenInsets.bottom,
            )
        }

        ConverterApp()
    }
    }
}
