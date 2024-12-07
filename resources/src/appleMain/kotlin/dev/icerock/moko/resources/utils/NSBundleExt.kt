/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.utils

import platform.Foundation.NSBundle
import platform.Foundation.NSDirectoryEnumerator
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.Foundation.pathExtension

fun NSBundle.Companion.loadableBundle(identifier: String): NSBundle {
    // at first we try to find required bundle inside Bundle.main, because it's faster way
    // https://github.com/icerockdev/moko-resources/issues/708
    // but in some cases (for example in SwiftUI Previews) dynamic framework with bundles can be located
    // in different location, not inside Bundle.main. So in this case we run less performant way - bundleWithIdentifier
    // https://github.com/icerockdev/moko-resources/issues/747
    return findBundleInMain(identifier)
        ?: NSBundle.bundleWithIdentifier(identifier)
        ?: throw IllegalArgumentException("bundle with identifier $identifier not found")
}

private fun findBundleInMain(identifier: String): NSBundle? {
    val bundlePath: String = NSBundle.mainBundle.bundlePath

    val enumerator: NSDirectoryEnumerator = requireNotNull(
        NSFileManager.defaultManager.enumeratorAtPath(bundlePath)
    ) { "can't get enumerator" }

    while (true) {
        val relativePath: String = enumerator.nextObject() as? String ?: break
        val url = NSURL(fileURLWithPath = relativePath)
        if (url.pathExtension == "bundle") {
            val fullPath = "$bundlePath/$relativePath"
            val foundedBundle: NSBundle? = NSBundle.bundleWithPath(fullPath)
            val loadedIdentifier: String? = foundedBundle?.bundleIdentifier

            if (isBundleSearchLogEnabled) {
                // NSLog to see this logs in Console app when debug SwiftUI previews or release apps
                NSLog("moko-resources auto-load bundle with identifier $loadedIdentifier at path $fullPath")
            }

            if (foundedBundle?.bundleIdentifier == identifier) return foundedBundle
        }
    }

    return null
}

var isBundleSearchLogEnabled = false
