/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

actual class AssetResource(
    resourcesClassLoader: ClassLoader,
    actual val originalPath: String,
    path: String
) : FileResource(resourcesClassLoader, path) {

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
