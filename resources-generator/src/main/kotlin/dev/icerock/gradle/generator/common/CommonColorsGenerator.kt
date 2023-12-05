/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.ColorNode
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.metadata.GeneratorType
import org.gradle.api.file.FileTree

class CommonColorsGenerator(
    ownColorsFileTree: FileTree,
    upperColorsFileTree: FileTree,
) : ColorsGenerator(ownColorsFileTree), ObjectBodyExtendable by NOPObjectBodyExtendable() {

    override fun getImports(): List<ClassName> {
        return emptyList()
    }

    override fun getPropertyInitializer(color: ColorNode): CodeBlock? = null
}
