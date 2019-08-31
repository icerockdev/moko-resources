package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

class IosGenerator(
    generatedDir: File,
    sourceSet: KotlinSourceSet,
    languagesStrings: Map<LanguageType, Map<KeyType, String>>,
    mrClassPackage: String
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

    override fun getImports(): Array<ClassName> {
        return super.getImports().plus(
            ClassName("platform.Foundation", "NSBundle")
        )
    }

    override fun getStringsPropertyInitializer(key: String): CodeBlock? {
        // FIXME read identifier from configs of project
        // TODO move bundle to companion object
        val bundle =
            "NSBundle.bundleWithIdentifier(\"com.icerockdev.library.MultiPlatformLibrary\")!!"
        return CodeBlock.of("StringResource(resourceId = %S, bundle = $bundle)", key)
    }

    override fun generateResources(language: String?, strings: Map<KeyType, String>) {
        val resDirName = when (language) {
            null -> "Base.lproj"
            else -> "$language.lproj"
        }

        val resDir = File(resourcesGenerationDir, resDirName)
        val localizableFile = File(resDir, "Localizable.strings")
        resDir.mkdirs()

        val content = strings.map { (key, value) ->
            "\"$key\" = \"$value\";"
        }.joinToString("\n")

        localizableFile.writeText(content)
    }

    override fun configureTasks(generationTask: Task, project: Project) {
        val linkTasks = project.tasks
            .mapNotNull { it as? KotlinNativeLink }
            .filter { it.binary is Framework }
            .filter { it.compilation.kotlinSourceSets.contains(sourceSet) }

        linkTasks.forEach { linkTask ->
            linkTask.dependsOn(generationTask)

            val framework = linkTask.binary as? Framework ?: return@forEach

            val pack =
                project.tasks.create(linkTask.name.replace("link", "pack"), Copy::class.java) {
                    group = "multiplatform"

                    from(resourcesGenerationDir)
                    into(framework.outputFile)
                }

            linkTask.finalizedBy(pack)
        }
    }
}