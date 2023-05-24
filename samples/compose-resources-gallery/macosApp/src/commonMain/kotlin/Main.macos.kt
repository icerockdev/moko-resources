import androidx.compose.ui.window.Window
import com.icerockdev.library.MR
import dev.icerock.moko.resources.desc.desc
import kotlinx.cinterop.staticCFunction
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSApplicationDelegateProtocol
import platform.darwin.NSObject
import platform.objc.objc_setUncaughtExceptionHandler

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) {
    setUnhandledExceptionHook {
        it.printStackTrace()
    }

    objc_setUncaughtExceptionHandler(staticCFunction<Any?, Unit> {
        println(it)
    })

    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

    app.delegate = object : NSObject(), NSApplicationDelegateProtocol {
        override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean {
            return true
        }
    }

    Window(MR.strings.hello_world.desc().localized()) {
        MacosApp()
    }

    app.run()
}
