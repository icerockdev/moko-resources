/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ColorNode
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.jsJvmCommon.createColorResourceInitializer
import org.gradle.api.Project
import org.gradle.api.file.FileTree

class JvmColorsGenerator(
    project: Project,
    resourcesFileTree: FileTree,
    mrClassName: String,
) : ColorsGenerator(resourcesFileTree),
    ObjectBodyExtendable by ClassLoaderExtender(mrClassName) {

    override fun getImports() = listOf(
        ClassName("dev.icerock.moko.graphics", "Color")
    )

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(color: ColorNode): CodeBlock {
        return createColorResourceInitializer(color)
    }
}
