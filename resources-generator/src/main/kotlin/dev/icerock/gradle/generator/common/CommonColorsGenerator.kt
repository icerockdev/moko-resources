/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.ColorNode
import dev.icerock.gradle.generator.ColorsGenerator
import org.gradle.api.file.FileTree

class CommonColorsGenerator(
    colorsFileTree: FileTree
) : ColorsGenerator(colorsFileTree) {

    override fun getImports(): List<ClassName> {
        return emptyList()
    }

    override fun getPropertyInitializer(color: ColorNode): CodeBlock? = null
}
