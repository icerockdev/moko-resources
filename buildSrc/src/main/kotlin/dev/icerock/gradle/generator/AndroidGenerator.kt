package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

class AndroidGenerator(
    generatedDir: File,
    sourceSet: KotlinSourceSet,
    languagesStrings: Map<LanguageType, Map<KeyType, String>>,
    mrClassPackage: String,
    private val androidRClassPackage: String
) : Generator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    languagesStrings = languagesStrings,
    mrClassPackage = mrClassPackage
) {
    private val resourcesGenerationDir = File(generatedDir, "${sourceSet.name}/res")

    init {
        sourceSet.resources.srcDir(resourcesGenerationDir)
    }

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getStringsClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getStringsPropertyModifiers(): Array<KModifier> = arrayOf(
        KModifier.ACTUAL
    )

    override fun getStringsPropertyInitializer(key: String): CodeBlock? {
        val processedKey = key.replace(".", "_")
        return CodeBlock.of("StringResource(R.string.%L)", processedKey)
    }

    override fun getImports(): Array<ClassName> = arrayOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateResources(language: String?, strings: Map<KeyType, String>) {
        val valuesDirName = when (language) {
            null -> "values"
            else -> "values-$language"
        }

        val valuesDir = File(resourcesGenerationDir, valuesDirName)
        val stringsFile = File(valuesDir, "multiplatform_strings.xml")
        valuesDir.mkdirs()

        val header = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
            """.trimIndent()

        val content = strings.map { (key, value) ->
            "\t<string name=\"$key\">$value</string>"
        }.joinToString("\n")

        val footer = """
</resources>
            """.trimIndent()

        stringsFile.writeText(header + "\n")
        stringsFile.appendText(content)
        stringsFile.appendText("\n" + footer)
    }

    override fun configureTasks(generationTask: Task, project: Project) {
        project.tasks.getByName("preBuild").dependsOn(generationTask)
    }
}
