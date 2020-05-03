/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.Foundation.NSBundle

fun NSBundle.Companion.loadableBundle(identifier: String): NSBundle {
    val result = NSBundle.bundleWithIdentifier(identifier)
    if (result != null) return result

    val mainPath = NSBundle.mainBundle.bundlePath
    val appFrameworks = NSBundle.allFrameworks.filterIsInstance<NSBundle>()
        .filter { it.bundlePath.startsWith(mainPath) }
    appFrameworks.flatMap { frameworkBundle ->
        @Suppress("UNCHECKED_CAST")
        frameworkBundle.pathsForResourcesOfType(ext = "bundle", inDirectory = null) as List<String>
    }.forEach { bundlePath ->
        // load each loadable bundle to correct load by identifier later
        NSBundle.bundleWithPath(bundlePath)?.bundleIdentifier
    }

    return NSBundle.bundleWithIdentifier(identifier)!!
}
