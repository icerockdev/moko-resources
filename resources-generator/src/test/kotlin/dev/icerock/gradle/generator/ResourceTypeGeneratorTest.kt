/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.resources.NOPResourceGenerator
import dev.icerock.gradle.generator.resources.string.StringResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.StringMetadata
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ResourceTypeGeneratorTest {

    @Test
    fun longResourceNamesKeepLazyLambdaOnTheSameLine() {
        val generator = ResourceTypeGenerator(
            propertiesGenerationStrategy = FlatPropertiesGenerationStrategy<StringMetadata>(),
            resourceClass = Constants.stringResourceName,
            resourceType = ResourceType.STRINGS,
            metadataClass = StringMetadata::class,
            visibilityModifier = KModifier.PUBLIC,
            generator = StringResourceGenerator(strictLineBreaks = false),
            platformResourceGenerator = NOPResourceGenerator(),
            filter = {},
            resourcesPackageName = "com.example.resources"
        )

        listOf(59, 60, 120).forEach { keyLength ->
            val key = "s".repeat(keyLength)
            val result = assertNotNull(
                generator.generateObject(
                    parentObjectName = "MR",
                    resources = listOf(
                        StringMetadata(
                            key = key,
                            values = listOf(StringMetadata.LocaleItem(locale = "base", value = "test"))
                        )
                    ),
                    sourceSetName = "commonMain"
                )
            )
            val generatedFile = result.fileSpecs.single().toString()
            val delegateLine = generatedFile.lineSequence().first { "lazy" in it }

            assertTrue("lazy {" in delegateLine, "Key length $keyLength: $delegateLine")
        }
    }
}
