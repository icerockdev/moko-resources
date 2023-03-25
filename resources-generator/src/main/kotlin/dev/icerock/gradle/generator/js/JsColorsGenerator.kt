/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.ColorNode
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.jsJvmCommon.createColorResourceInitializer
import org.gradle.api.file.FileTree

class JsColorsGenerator(
    colorsFileTree: FileTree
) : ColorsGenerator(colorsFileTree), ObjectBodyExtendable by NOPObjectBodyExtendable() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getImports() = listOf(
        ClassName("dev.icerock.moko.graphics", "Color")
    )

    override fun beforeGenerate(objectBuilder: TypeSpec.Builder, keys: List<String>) {
        val languageKeysList = keys.joinToString()

        objectBuilder.addFunction(
            FunSpec.builder("values")
                .addModifiers(KModifier.OVERRIDE)
                .addStatement("return listOf($languageKeysList)")
                .returns(
                    ClassName("kotlin.collections", "List")
                        .parameterizedBy(resourceClassName)
                )
                .build()
        )
    }

    override fun getPropertyInitializer(color: ColorNode): CodeBlock {
        return createColorResourceInitializer(color)
    }
}
