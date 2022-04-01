import platform.UIKit.*

class AppDelegate : UIResponder, UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    @OverrideInit
    constructor() : super()

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    override fun application(application: UIApplication, didFinishLaunchingWithOptions: Map<Any?, *>?): Boolean {
        window = UIWindow(frame = UIScreen.mainScreen.bounds).apply {
            screen = UIScreen.mainScreen
            val storyboard = UIStoryboard.storyboardWithName(name = "Main", bundle = null)
            val initialViewController = storyboard.instantiateViewControllerWithIdentifier(identifier = "LabelViewController")
            rootViewController = initialViewController
            makeKeyAndVisible()
        }
        return true
    }
}
