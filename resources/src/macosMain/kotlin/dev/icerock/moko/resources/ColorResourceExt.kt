/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.AppKit.NSColor

fun ColorResource.getNSColor(): NSColor {
    return NSColor.colorNamed(
        name = this.name,
        bundle = this.bundle
    ) ?: throw IllegalStateException(
        "Can't read color $name from bundle $bundle, " +
                "please check moko-resources gradle configuration"
    )
}
