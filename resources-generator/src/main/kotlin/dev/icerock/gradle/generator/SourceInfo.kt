/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import org.gradle.api.file.SourceDirectorySet
import java.io.File

data class SourceInfo(
    val generatedDir: File,
    val commonResources: SourceDirectorySet,
    val mrClassPackage: String,
    val androidRClassPackage: String
){
    private var _androidRClassPackage: String? = null

    val androidRClassPackage: String get() = _androidRClassPackage!!

    fun setAndroidRClassPackage(value: String) {
        if (_androidRClassPackage != null) throw IllegalStateException("Android R class already set")

        _androidRClassPackage = value
    }
}

