package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.StringsGenerator
import org.gradle.api.file.FileTree
import java.io.File

const val BUNDLE_NAME = "MokoBundle"
const val PLURALS_BUNDLE_NAME = "MokoPluralsBundle"
const val LOCALIZATION_DIR = "localization"

class JvmStringsGenerator(stringsFileTree: FileTree) : StringsGenerator(stringsFileTree) {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String, baseLanguageMap: Map<KeyType, String>) =
        CodeBlock.of("StringResource(%S)", key)

    override fun generateResources(
        resourcesGenerationDir: File,
        language: String?,
        strings: Map<KeyType, String>
    ) {
        val fileDirName = when (language) {
            null -> BUNDLE_NAME
            else -> "${BUNDLE_NAME}_$language"
        }

        val localizationDir = File(resourcesGenerationDir, LOCALIZATION_DIR).apply {
            mkdirs()
        }
        val stringsFile = File(localizationDir, "${fileDirName}.properties")

        val content = strings.map { (key, value) ->
            "$key = ${value.replaceAndroidFormatParameters()}"
        }.joinToString("\n")

        stringsFile.writeText(content)
    }

    companion object {
        private val androidFormatRegex = "%.(\\$.)?".toRegex()

        fun String.replaceAndroidFormatParameters(): String {

            var formattedValue = this
            var paramNr = 0

            while (androidFormatRegex.containsMatchIn(formattedValue)) {
                formattedValue = formattedValue.replaceFirst(androidFormatRegex, "{${paramNr++}}")
            }
            return formattedValue
        }
    }
}