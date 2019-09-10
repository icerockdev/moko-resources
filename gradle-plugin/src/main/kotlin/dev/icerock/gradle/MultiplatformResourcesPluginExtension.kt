/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

open class MultiplatformResourcesPluginExtension {
    var multiplatformResourcesPackage: String? = null
        set(value) {
            field = value
            onChange?.invoke()
        }

    internal var onChange: (() -> Unit)? = null
}