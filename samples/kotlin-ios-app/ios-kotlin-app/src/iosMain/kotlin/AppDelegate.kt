import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UIApplicationDelegateProtocolMeta
import platform.UIKit.UIResponder
import platform.UIKit.UIResponderMeta
import platform.UIKit.UIScreen
import platform.UIKit.UIStoryboard
import platform.UIKit.UIWindow

class AppDelegate : UIResponder, UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    @OverrideInit
    constructor() : super()

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: Map<Any?, *>?
    ): Boolean {
        window = UIWindow(frame = UIScreen.mainScreen.bounds).apply {
            screen = UIScreen.mainScreen
            val storyboard = UIStoryboard.storyboardWithName(name = "Main", bundle = null)
            val initialViewController = storyboard.instantiateViewControllerWithIdentifier(
                identifier = "LabelViewController"
            )
            rootViewController = initialViewController
            makeKeyAndVisible()
        }
        return true
    }
}
