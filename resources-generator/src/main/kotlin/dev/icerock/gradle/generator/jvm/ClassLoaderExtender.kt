/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.provider.Provider

class ClassLoaderExtender(private val mrClassName: String) : ObjectBodyExtendable {
    override fun extendObjectBodyAtStart(classBuilder: TypeSpec.Builder) {
        classBuilder.addProperty(
            PropertySpec.builder(
                "resourcesClassLoader",
                ClassName("java.lang", "ClassLoader"),
                KModifier.OVERRIDE
            )
                .initializer(CodeBlock.of("${mrClassName}::class.java.classLoader"))
                .build()
        )
    }

    override fun extendObjectBodyAtEnd(classBuilder: TypeSpec.Builder) = Unit
}
