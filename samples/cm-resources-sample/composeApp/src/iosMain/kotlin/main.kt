import androidx.compose.ui.window.ComposeUIViewController
import com.icerock.cm.sample.app.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
