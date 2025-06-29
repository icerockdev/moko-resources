import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Wasm Web"

@Composable
fun MainView() = App()
