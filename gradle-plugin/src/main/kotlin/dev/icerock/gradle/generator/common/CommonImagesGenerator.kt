/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ImagesGenerator
import org.gradle.api.file.FileTree

class CommonImagesGenerator(
    inputFileTree: FileTree
) : ImagesGenerator(
    inputFileTree = inputFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = emptyArray()

    override fun getPropertyModifiers(): Array<KModifier> = emptyArray()

    override fun getPropertyInitializer(key: String): CodeBlock? = null

    override fun buildGetByFileNameMethod(builder: FunSpec.Builder): FunSpec.Builder {
        return builder
    }
}
