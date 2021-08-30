/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.Foundation.NSBundle

actual class AssetResource(
    fileName: String,
    extension: String,
    bundle: NSBundle = NSBundle.mainBundle
) : FileResource(fileName, extension, bundle)