package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.PluralMap
import dev.icerock.gradle.generator.PluralsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class JvmPluralsGenerator(pluralsFileTree: FileTree) : PluralsGenerator(pluralsFileTree) {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String, baseLanguageMap: Map<KeyType, PluralMap>) =
        CodeBlock.of(
            "PluralsResource(%S, %L)",
            key,
            getNumberFormat(baseLanguageMap)
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

        val stringsFile = File(resourcesGenerationDir, "${fileDirName}.properties")
        resourcesGenerationDir.mkdirs()

        val content = strings.map { (key, pluralMap) ->
            "$key = {0}\n" +
                    pluralMap.map { (quantity, value) ->
                        "${getQuantityKey(key = key, quantity = quantity)} = $value"
                    }.joinToString("\n")
        }.joinToString("\n")

        stringsFile.writeText(content)
    }

    private fun getNumberFormat(baseLanguageMap: Map<KeyType, PluralMap>) =
        "mapOf(${
            baseLanguageMap.map { (key, pluralMap) ->
                "\"$key\" to listOf(${
                    pluralMap.map { (quantity, _) ->
                        val quantityInNumber = mapAndroidQuantityToDouble(quantity)
                            ?: throw IllegalArgumentException("quantity $quantity is not conforming to the Android standards")

                        "${quantityInNumber.toDouble()} to \"${
                            getQuantityKey(
                                key = key,
                                quantity = quantity
                            )
                        }\""
                    }.joinToString()
                })"
            }.joinToString()
        })"

    private fun getQuantityKey(key: String, quantity: String) =
        "${key}_${QUANTITY_PREFIX}_${mapAndroidQuantityToDouble(quantity)}"

    // TODO handle other type
    private fun mapAndroidQuantityToDouble(quantity: String) = when (quantity) {
        "zero" -> 0
        "one" -> 1
        "two" -> 2
        "few" -> 6
        "many" -> 12
        "other" -> -1
        else -> null
    }

    companion object {
        private const val QUANTITY_PREFIX = "quantity"
    }
}