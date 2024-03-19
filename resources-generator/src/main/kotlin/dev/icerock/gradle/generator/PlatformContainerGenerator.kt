/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

internal interface PlatformContainerGenerator {
    fun getImports(): List<ClassName> = emptyList()
    fun generateBeforeTypes(objectName: String, builder: TypeSpec.Builder) = Unit
    fun generateAfterTypes(builder: TypeSpec.Builder) = Unit
}
