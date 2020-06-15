/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

actual fun ResourceContainer<ImageResource>.getImageByFileName(fileName: String): ImageResource? {
    if(fileName.isBlank()) return null

    val lastNamePart = if (fileName.length > 1) {
        fileName.substring(1, fileName.length)
    } else ""
    val methodName = StringBuilder()
        .append("get")
        .append(fileName[0].toUpperCase())
        .append(lastNamePart).toString()

    return this::class.java.methods
        .find { it.name == methodName }
        ?.let { it.invoke(this) as ImageResource }
}
