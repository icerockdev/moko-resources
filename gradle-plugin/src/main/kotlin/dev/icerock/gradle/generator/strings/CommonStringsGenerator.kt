/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.strings

import com.squareup.kotlinpoet.CodeBlock
import org.gradle.api.file.FileTree

class CommonStringsGenerator(
    stringsFileTree: FileTree
) : StringsGenerator(
    stringsFileTree = stringsFileTree
) {
    override fun getPropertyInitializer(key: String): CodeBlock? = null
}
