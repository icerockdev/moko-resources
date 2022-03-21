/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.Foundation.NSBundle

actual class StringResource(
    val resourceId: String,
    val bundle: NSBundle = NSBundle.mainBundle
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StringResource) return false
        if (resourceId != other.resourceId) return false
        return true
    }

    override fun hashCode(): Int =
        resourceId.hashCode()
}
