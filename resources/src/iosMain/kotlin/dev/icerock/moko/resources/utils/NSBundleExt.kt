/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.utils

import platform.Foundation.NSBundle
import platform.Foundation.NSDirectoryEnumerator
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.pathExtension

fun NSBundle.Companion.loadableBundle(identifier: String): NSBundle {
    // try get already loaded bundle
    NSBundle.bundleWithIdentifier(identifier)?.let { return it }

    val bundlePath: String = NSBundle.mainBundle.bundlePath
    val enumerator: NSDirectoryEnumerator = requireNotNull(NSFileManager.defaultManager.enumeratorAtPath(bundlePath))
    while (true) {
        val relativePath: String = enumerator.nextObject() as? String ?: break
        val url = NSURL(fileURLWithPath = relativePath)
        if (url.pathExtension == "bundle") {
            val fullPath = "$bundlePath/$relativePath"
            val loadedIdentifier: String? = NSBundle.bundleWithPath(fullPath)?.bundleIdentifier
            if(isBundleSearchLogEnabled) {
                println("moko-resources auto-load bundle with identifier $loadedIdentifier at path $fullPath")
            }
        }
    }

    val resultBundle = NSBundle.bundleWithIdentifier(identifier)
    if (resultBundle == null) {
        throw IllegalArgumentException("bundle with identifier $identifier not found")
    }

    return resultBundle
}

var isBundleSearchLogEnabled = false
