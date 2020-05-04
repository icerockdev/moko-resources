/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import android.content.res.Resources
import androidx.annotation.RawRes
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

actual class FileResource(
    @RawRes
    val rawResId: Int
) {
    fun readText(context: Context): String {
        val resources: Resources = context.resources
        val inputStream: InputStream = resources.openRawResource(rawResId)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return bufferedReader.readText()
    }
}
