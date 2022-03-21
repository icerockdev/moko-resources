/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.Foundation.NSBundle
import platform.Foundation.NSURL

actual class AssetResource(
    actual val originalPath: String,
    fileName: String,
    extension: String,
    bundle: NSBundle = NSBundle.mainBundle
) : FileResource(fileName, extension, bundle) {
    override val path: String
        get() = bundle.pathForResource(
            name = fileName,
            ofType = extension
        )!!
    override val url: NSURL
        get() = bundle.URLForResource(
            name = fileName,
            withExtension = extension
        )!!

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AssetResource) return false

        if (originalPath != other.originalPath) return false

        return true
    }

    override fun hashCode(): Int {
        return originalPath.hashCode()
    }
}
