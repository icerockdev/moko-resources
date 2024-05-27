import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    CanvasBasedWindow("Js App") {
        MainView()
    }
}