package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.StringsGenerator
import org.gradle.api.file.FileTree
import java.io.File

// TODO should be shared with MokoBundle.Bundle_NAME
const val BUNDLE_NAME = "moko.MokoBundle"
const val PLURALS_BUNDLE_NAME = "moko.MokoPluralsBundle"

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

        val stringsFile = File(resourcesGenerationDir, "${fileDirName}.properties")
        resourcesGenerationDir.mkdirs()

        val content = strings.map { (key, value) ->
            "$key = $value"
        }.joinToString("\n")

        stringsFile.writeText(content)
    }
}