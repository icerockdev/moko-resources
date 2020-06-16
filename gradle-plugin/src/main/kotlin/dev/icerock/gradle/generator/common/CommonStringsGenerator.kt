/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.StringsGenerator
import org.gradle.api.file.FileTree

class CommonStringsGenerator(
    stringsFileTree: FileTree
) : StringsGenerator(
    stringsFileTree = stringsFileTree
) {
    override fun getPropertyInitializer(key: String): CodeBlock? = null
}
