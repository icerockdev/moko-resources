/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.plurals

import com.squareup.kotlinpoet.KModifier
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class IosPluralsGenerator(
    sourceSet: KotlinSourceSet,
    pluralsFileTree: FileTree
) : PluralsGenerator(
    sourceSet = sourceSet,
    pluralsFileTree = pluralsFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
}