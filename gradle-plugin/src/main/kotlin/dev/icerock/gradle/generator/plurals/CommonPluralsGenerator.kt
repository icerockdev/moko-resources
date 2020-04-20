/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.plurals

import com.squareup.kotlinpoet.CodeBlock
import org.gradle.api.file.FileTree

class CommonPluralsGenerator(
    pluralsFileTree: FileTree
) : PluralsGenerator(
    pluralsFileTree = pluralsFileTree
) {
    override fun getPropertyInitializer(key: String): CodeBlock? = null
}
