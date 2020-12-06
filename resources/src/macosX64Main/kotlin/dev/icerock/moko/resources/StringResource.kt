/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.Foundation.NSBundle

actual class StringResource(
    val resourceId: String,
    val bundle: NSBundle = NSBundle.mainBundle
)
