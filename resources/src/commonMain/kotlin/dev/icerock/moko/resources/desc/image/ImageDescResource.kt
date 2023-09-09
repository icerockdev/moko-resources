/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.image

import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.resources.ImageResource

@Parcelize
data class ImageDescResource(val resource: ImageResource) : ImageDesc

@Suppress("FunctionName")
fun ImageDesc.Companion.Resource(resource: ImageResource): ImageDesc = ImageDescResource(resource)

fun ImageResource.asImageDesc() = ImageDesc.Resource(this)
