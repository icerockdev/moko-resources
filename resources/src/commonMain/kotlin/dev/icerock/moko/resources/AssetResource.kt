/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

expect class AssetResource {
    val originalPath: String
}

internal fun String.removeFirstSlash(): String {

    val originalPath: String = if (this.startsWith('/')) {
        this.substring(1)
    } else {
        this
    }
    return originalPath
}
