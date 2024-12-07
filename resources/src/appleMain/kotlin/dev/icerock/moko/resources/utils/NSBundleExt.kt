/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.utils

import dev.icerock.moko.resources.apple.native.ResourcesBundleAnchor
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDirectoryEnumerator
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.Foundation.pathExtension

fun NSBundle.Companion.loadableBundle(identifier: String): NSBundle {
    // we should use search by our class because dynamic framework with resources can be placed in
    //  external directory, not inside app directory (NSBundle.main). for example in case of
    //  SwiftUI preview - app directory empty, but dynamic framework with resources will be in
    //  different directory (DerivedData)
    @OptIn(ExperimentalForeignApi::class)
    val rootBundle: NSBundle = requireNotNull(ResourcesBundleAnchor.getResourcesBundle()) {
        "root NSBundle can't be found"
    }
    val bundlePath: String = rootBundle.bundlePath

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

    throw IllegalArgumentException("bundle with identifier $identifier not found")
}

var isBundleSearchLogEnabled = false
