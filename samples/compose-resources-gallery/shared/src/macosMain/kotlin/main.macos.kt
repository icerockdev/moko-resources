import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "macOS"

@Composable
fun MacosApp() {
    App()
}
