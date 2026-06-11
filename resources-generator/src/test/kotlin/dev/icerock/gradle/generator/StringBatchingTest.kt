/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.resources.string.AndroidStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.AppleStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.StringResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.StringMetadata
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringBatchingTest {
    @Test
    fun commonStringsAreGeneratedAsExtensionAccessors() {
        val result = createCommonGenerator().generateExpectObject(
            parentObjectName = MR_CLASS_NAME,
            resources = createStrings(count = 1),
            sourceSetName = COMMON_SOURCE_SET
        )!!

        val containerSource = result.typeSpec.toString()
        assertFalse(containerSource.contains("val key_000: StringResource"), containerSource)
        assertTrue(
            result.typeSpec.propertySpecs.any { it.name == "__platformDetails" },
            containerSource
        )
        assertTrue(
            containerSource.contains("public override val __platformDetails"),
            containerSource
        )
        assertTrue(
            result.typeSpec.funSpecs.any { it.name == "values" },
            containerSource
        )

        assertEquals(expected = 1, actual = result.fileSpecs.size)
        assertEquals(expected = "CommonMainStrings0.commonMain", actual = result.fileSpecs.single().name)

        val accessorSource = result.fileSpecs.single().toString()
        assertTrue(
            accessorSource.contains("public expect val MR.strings.key_000: StringResource"),
            accessorSource
        )
        assertFalse(accessorSource.contains("CommonMainStrings0"), accessorSource)
    }

    @Test
    fun commonStringsAreSplitIntoFiftyItemBatches() {
        val oneHundredResult = createCommonGenerator().generateExpectObject(
            parentObjectName = MR_CLASS_NAME,
            resources = createStrings(count = 100),
            sourceSetName = COMMON_SOURCE_SET
        )!!
        val oneHundredOneResult = createCommonGenerator().generateExpectObject(
            parentObjectName = MR_CLASS_NAME,
            resources = createStrings(count = 101),
            sourceSetName = COMMON_SOURCE_SET
        )!!
        val largeResult = createCommonGenerator().generateExpectObject(
            parentObjectName = MR_CLASS_NAME,
            resources = createStrings(count = 1_000),
            sourceSetName = COMMON_SOURCE_SET
        )!!

        assertEquals(expected = 2, actual = oneHundredResult.fileSpecs.size)
        assertEquals(
            expected = listOf(
                "CommonMainStrings0.commonMain",
                "CommonMainStrings1.commonMain",
                "CommonMainStrings2.commonMain"
            ),
            actual = oneHundredOneResult.fileSpecs.map { it.name }
        )
        assertEquals(expected = 20, actual = largeResult.fileSpecs.size)
        assertEquals(expected = "CommonMainStrings0.commonMain", actual = largeResult.fileSpecs.first().name)
        assertEquals(expected = "CommonMainStrings19.commonMain", actual = largeResult.fileSpecs.last().name)
    }

    @Test
    fun androidActualStringsUseInternalBatchesAndAggregatedValues() {
        val commonResult = createCommonGenerator().generateExpectObject(
            parentObjectName = MR_CLASS_NAME,
            resources = createStrings(count = 101),
            sourceSetName = COMMON_SOURCE_SET
        )!!
        val androidResult = createAndroidGenerator().generateActualObject(
            parentObjectName = MR_CLASS_NAME,
            objects = listOf(commonResult.metadata),
            sourceSetName = ANDROID_SOURCE_SET
        )!!

        val containerSource = androidResult.typeSpec.toString()
        assertFalse(containerSource.contains("val key_000: StringResource"), containerSource)
        assertTrue(containerSource.contains("CommonMainStrings0.values()"), containerSource)
        assertTrue(containerSource.contains("CommonMainStrings1.values()"), containerSource)
        assertTrue(containerSource.contains("CommonMainStrings2.values()"), containerSource)

        val firstBatchSource = androidResult.fileSpecs
            .single { it.name == "CommonMainStrings0.androidMain" }
            .toString()
        assertTrue(
            firstBatchSource.contains("internal object CommonMainStrings0"),
            firstBatchSource
        )
        assertTrue(
            firstBatchSource.contains("internal val key_000: StringResource by"),
            firstBatchSource
        )
        assertTrue(firstBatchSource.contains("private fun init_key_000(): StringResource"), firstBatchSource)
        assertTrue(firstBatchSource.contains("StringResource(R.string.key_000)"), firstBatchSource)
        assertTrue(
            firstBatchSource.contains("public actual val MR.strings.key_000: StringResource"),
            firstBatchSource
        )

        val secondBatchSource = androidResult.fileSpecs
            .single { it.name == "CommonMainStrings2.androidMain" }
            .toString()
        assertTrue(secondBatchSource.contains("StringResource(R.string.key_100)"), secondBatchSource)
    }

    @Test
    fun appleActualStringsUseSharedPlatformDetailsProvider() {
        val commonResult = createCommonGenerator().generateExpectObject(
            parentObjectName = MR_CLASS_NAME,
            resources = createStrings(count = 1),
            sourceSetName = COMMON_SOURCE_SET
        )!!
        val appleResult = createAppleGenerator().generateActualObject(
            parentObjectName = MR_CLASS_NAME,
            objects = listOf(commonResult.metadata),
            sourceSetName = IOS_SOURCE_SET
        )!!

        val containerSource = appleResult.typeSpec.toString()
        assertTrue(containerSource.contains("public actual override val __platformDetails"), containerSource)
        assertTrue(containerSource.contains("PlatformDetailsProvider.details"), containerSource)

        val batchSource = appleResult.fileSpecs
            .single { it.name == "CommonMainStrings0.iosMain" }
            .toString()
        assertTrue(
            batchSource.contains("internal object CommonMainStrings0"),
            batchSource
        )
        assertTrue(batchSource.contains("internal val key_000: StringResource by"), batchSource)
        assertTrue(
            batchSource.contains("resourceId = \"key_000\""),
            batchSource
        )
        assertTrue(
            batchSource.contains("PlatformDetailsProvider.details.nsBundle"),
            batchSource
        )
        assertFalse(batchSource.contains("private val bundle"), batchSource)
        assertFalse(batchSource.contains("private val __platformDetails"), batchSource)
        assertTrue(batchSource.contains("public actual val MR.strings.key_000: StringResource"), batchSource)
    }

    @Test
    fun targetSpecificStringsUseCorrectVisibility() {
        val result = createAndroidGenerator().generateObject(
            parentObjectName = "MRandroidMain",
            resources = createStrings(count = 1),
            sourceSetName = ANDROID_SOURCE_SET
        )!!

        val batchSource = result.fileSpecs
            .single { it.name == "AndroidMainStrings0.androidMain" }
            .toString()

        // Batch object should be public (visibilityModifier)
        assertTrue(batchSource.contains("internal object AndroidMainStrings0"), batchSource)
        // Extension property should be public
        assertTrue(batchSource.contains("public val MRandroidMain.strings.key_000: StringResource"), batchSource)
    }

    private fun createCommonGenerator(): ResourceTypeGenerator<StringMetadata> {
        return createGenerator(platformResourceGenerator = EmptyStringPlatformResourceGenerator())
    }

    private fun createAndroidGenerator(): ResourceTypeGenerator<StringMetadata> {
        return createGenerator(
            platformResourceGenerator = AndroidStringResourceGenerator(
                androidRClassPackage = "com.example",
                resourcesGenerationDir = File(".")
            )
        )
    }

    private fun createAppleGenerator(): ResourceTypeGenerator<StringMetadata> {
        return createGenerator(
            platformResourceGenerator = AppleStringResourceGenerator(
                baseLocalizationRegion = "en",
                resourcesGenerationDir = File(".")
            )
        )
    }

    private fun createGenerator(
        platformResourceGenerator: PlatformResourceGenerator<StringMetadata>,
    ): ResourceTypeGenerator<StringMetadata> {
        return ResourceTypeGenerator(
            propertiesGenerationStrategy = FlatPropertiesGenerationStrategy(),
            resourceClass = Constants.stringResourceName,
            resourceType = ResourceType.STRINGS,
            metadataClass = StringMetadata::class,
            visibilityModifier = KModifier.PUBLIC,
            generator = StringResourceGenerator(strictLineBreaks = false),
            platformResourceGenerator = platformResourceGenerator,
            filter = {},
            resourcesPackageName = PACKAGE_NAME
        )
    }

    private fun createStrings(count: Int): List<StringMetadata> {
        return (0 until count).map { index ->
            val key = "key_${index.toString().padStart(length = 3, padChar = '0')}"
            StringMetadata(
                key = key,
                values = listOf(
                    StringMetadata.LocaleItem(
                        locale = "",
                        value = key
                    )
                )
            )
        }
    }

    private class EmptyStringPlatformResourceGenerator : PlatformResourceGenerator<StringMetadata> {
        override fun imports(): List<ClassName> = emptyList()
        override fun generateInitializer(metadata: StringMetadata): CodeBlock = CodeBlock.of("")
        override fun generateResourceFiles(data: List<StringMetadata>) = Unit
    }

    private companion object {
        const val PACKAGE_NAME = "com.example.generated"
        const val MR_CLASS_NAME = "MR"
        const val COMMON_SOURCE_SET = "commonMain"
        const val ANDROID_SOURCE_SET = "androidMain"
        const val IOS_SOURCE_SET = "iosMain"
    }
}
