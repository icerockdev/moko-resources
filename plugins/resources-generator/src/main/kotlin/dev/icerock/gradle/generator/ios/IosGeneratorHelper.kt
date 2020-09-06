/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.ios

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.ObjectBodyExtendable

class IosGeneratorHelper(private val propertyName: String = "nsBundle") :
    ObjectBodyExtendable {

    override fun extendObjectBody(classBuilder: TypeSpec.Builder) {
        val bundleType = ClassName("platform.Foundation", "NSBundle")
        val bundleProperty = PropertySpec.builder(propertyName, bundleType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(IosMRGenerator.BUNDLE_PROPERTY_NAME)
            .build()

        classBuilder.addProperty(bundleProperty)
    }
}
