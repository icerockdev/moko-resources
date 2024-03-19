/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.parcelize.Parcelable

actual data class ImageResource(val fileName: String, val fileUrl: String) : Parcelable
