package dev.icerock.moko.resources

import dev.icerock.moko.resources.utils.loadableBundle
import platform.Foundation.NSBundle

object MokoResourcesBundle {
    val bundle: NSBundle by lazy { NSBundle.loadableBundle("com.icerockdev.library.MR") }
}