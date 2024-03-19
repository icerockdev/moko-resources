/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinNativeCompile
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal val KotlinNativeLink.klibs: FileCollection
    get() {
        return try {
            val getLibraries =
                AbstractKotlinNativeCompile::class.java.getDeclaredMethod("getLibraries")
            val getIntermediateLibrary =
                KotlinNativeLink::class.java.getDeclaredMethod("getSource")

            @Suppress("UNCHECKED_CAST")
            val libs: FileCollection = getLibraries.invoke(this) as FileCollection
            val library: FileCollection = getIntermediateLibrary.invoke(this) as FileCollection

            libs.plus(library)
        } catch (@Suppress("SwallowedException") exc: NoSuchMethodException) {
            libraries.plus(sources)
        }
    }

internal val Kotlin2JsCompile.klibs: FileCollection
    get() {
        return try {
            val getClasspath =
                AbstractKotlinCompileTool::class.java.getDeclaredMethod("getClasspath")

            val libs: FileCollection = getClasspath.invoke(this) as FileCollection
            libs
        } catch (@Suppress("SwallowedException") exc: NoSuchMethodException) {
            libraries.plus(sources)
        }
    }
