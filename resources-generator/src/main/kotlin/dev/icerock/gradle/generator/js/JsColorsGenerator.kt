/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.ColorsGenerator
import org.gradle.api.file.FileTree

class JsColorsGenerator(
    colorsFileTree: FileTree
) : ColorsGenerator(colorsFileTree) {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getImports(): List<ClassName> {
        return emptyList()
    }

    override fun extendObjectBodyAtStart(classBuilder: TypeSpec.Builder) {

    }

    override fun extendObjectBodyAtEnd(classBuilder: TypeSpec.Builder) {

    }
}