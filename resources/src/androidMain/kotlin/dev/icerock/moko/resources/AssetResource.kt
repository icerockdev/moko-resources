/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import android.os.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

@Parcelize
actual class AssetResource(val path: String) : Parcelable {

    fun getInputStream(context: Context): InputStream {
        return context.assets.open(path)
    }

    fun readText(context: Context): String {
        val inputStream: InputStream = getInputStream(context)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return bufferedReader.use { it.readText() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AssetResource) return false

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    @IgnoredOnParcel
    actual val originalPath: String by ::path
}
