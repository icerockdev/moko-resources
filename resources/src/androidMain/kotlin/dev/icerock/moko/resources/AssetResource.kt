/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

actual class AssetResource(val path: String) {

    fun readText(context: Context): String {
        val inputStream: InputStream = context.assets.open(path)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return bufferedReader.use { it.readText() }
    }
}
