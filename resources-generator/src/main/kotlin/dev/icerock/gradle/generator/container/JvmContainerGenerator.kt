/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.container

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformContainerGenerator
import dev.icerock.gradle.generator.addJvmClassLoaderProperty

internal class JvmContainerGenerator : PlatformContainerGenerator {
    override fun getImports(): List<ClassName> {
        return listOf(Constants.Jvm.classLoaderName)
    }

    override fun generateBeforeTypes(objectName: String, builder: Builder) {
        builder.addJvmClassLoaderProperty(objectName)
    }
}
