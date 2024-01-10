/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.container

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformContainerGenerator

internal class JsContainerGenerator : PlatformContainerGenerator {
    override fun generateAfterTypes(builder: TypeSpec.Builder) {
        val loaders: List<String> = builder.typeSpecs
            .flatMap { it.typeSpecs }
            .mapNotNull { typeSpec ->
                val loaderProperty = typeSpec.propertySpecs
                    .singleOrNull { it.name == Constants.Js.stringsLoaderPropertyName }
                    ?: return@mapNotNull null
                val objectName = typeSpec.name ?: return@mapNotNull null

                objectName + "." + loaderProperty.name
            }

        if (loaders.size > 1) {
            val initializer: String = loaders.joinToString(separator = " + ")

            val prop: PropertySpec = PropertySpec.builder(
                Constants.Js.stringsLoaderPropertyName,
                Constants.Js.stringLoaderName
            ).initializer(initializer).build()

            builder.addProperty(prop)
        }
    }
}
