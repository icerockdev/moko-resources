/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.PluralsGenerator
import org.gradle.api.file.FileTree

class CommonPluralsGenerator(
    pluralsFileTree: FileTree
) : PluralsGenerator(
    pluralsFileTree = pluralsFileTree
) {
    override fun getPropertyInitializer(key: String): CodeBlock? = null
}
