package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.PluralMap
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.jvm.JvmStringsGenerator.Companion.replaceAndroidFormatParameters
import org.gradle.api.file.FileTree
import java.io.File

class JvmPluralsGenerator(pluralsFileTree: FileTree) : PluralsGenerator(pluralsFileTree) {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String, baseLanguageMap: Map<KeyType, PluralMap>) =
        CodeBlock.of(
            "PluralsResource(%S, %L)",
            key,
            getNumberFormat(key, baseLanguageMap)
        )

    override fun generateResources(
        resourcesGenerationDir: File,
        language: String?,
        strings: Map<KeyType, PluralMap>
    ) {
        val fileDirName = when (language) {
            null -> PLURALS_BUNDLE_NAME
            else -> "${PLURALS_BUNDLE_NAME}_$language"
        }

        val localizationDir = File(resourcesGenerationDir, LOCALIZATION_DIR).apply { mkdirs() }
        val stringsFile = File(localizationDir, "${fileDirName}.properties")

        val content = strings.map { (key, pluralMap) ->
            "$key = {0}\n" +
                    pluralMap.map { (quantity, value) ->
                        "${
                            getQuantityKey(
                                key = key,
                                quantity = quantity
                            )
                        } = ${value.replaceAndroidFormatParameters()}"
                    }.joinToString("\n")
        }.joinToString("\n")

        stringsFile.writeText(content)
    }

    private fun getNumberFormat(key: String, baseLanguageMap: Map<KeyType, PluralMap>) =
        baseLanguageMap[key]?.let {
            "listOf(${
                it.mapNotNull { (quantity, _) ->
                    val quantityInNumber =
                        mapAndroidQuantityToDouble(quantity) ?: return@mapNotNull null

                    "${quantityInNumber.toDouble()} to \"${
                        getQuantityKey(
                            key = key,
                            quantity = quantity
                        )
                    }\""
                }.joinToString()
            })"
        }

    private fun getQuantityKey(key: String, quantity: String) =
        "${key}_${QUANTITY_PREFIX}_${mapAndroidQuantityToDouble(quantity)}"

    // Other will be skipped
    private fun mapAndroidQuantityToDouble(quantity: String) = when (quantity) {
        "zero" -> 0
        "one" -> 1
        "two" -> 2
        "few" -> 3
        "many" -> 6
        else -> null
    }

    companion object {
        private const val QUANTITY_PREFIX = "quantity"
    }
}