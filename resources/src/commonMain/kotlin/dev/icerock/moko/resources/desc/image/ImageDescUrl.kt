/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.image

import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class ImageDescUrl(val url: String) : ImageDesc

@Suppress("FunctionName")
fun ImageDesc.Companion.Url(url: String): ImageDesc = ImageDescUrl(url)

fun String.asImageUrl(): ImageDesc = ImageDesc.Url(this)
