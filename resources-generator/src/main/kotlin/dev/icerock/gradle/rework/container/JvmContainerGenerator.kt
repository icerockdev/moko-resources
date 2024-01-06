/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework.container

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.rework.CodeConst
import dev.icerock.gradle.rework.PlatformContainerGenerator
import dev.icerock.gradle.rework.addJvmClassLoaderProperty

class JvmContainerGenerator(
    private val resourcesClassName: String,
    private val flattenClassPackage: String
) : PlatformContainerGenerator {
    override fun getImports(): List<ClassName> {
        return listOf(CodeConst.Jvm.classLoaderClass)
    }

    override fun generateBeforeTypes(builder: TypeSpec.Builder) {
        builder.addJvmClassLoaderProperty(resourcesClassName)
    }
}
