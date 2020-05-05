/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.utils

import platform.Foundation.NSBundle

fun NSBundle.Companion.loadableBundle(identifier: String): NSBundle {
    // try get already loaded bundle
    NSBundle.bundleWithIdentifier(identifier)?.let { return it }

    // try load from app framework
    NSBundle.mainBundle
        .pathsForResourcesOfType(ext = "framework", inDirectory = "Frameworks")
        .filterIsInstance<String>()
        .mapNotNull { NSBundle.bundleWithPath(it) }
        .flatMap { it.pathsForResourcesOfType(ext = "bundle", inDirectory = null) }
        .filterIsInstance<String>()
        // load each loadable bundle to correct load by identifier later
        .forEach { NSBundle.bundleWithPath(it)?.bundleIdentifier }

    return NSBundle.bundleWithIdentifier(identifier)!!
}
