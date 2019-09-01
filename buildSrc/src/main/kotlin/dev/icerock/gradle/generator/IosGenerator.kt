package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.*
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
    private val bundleClassName = ClassName("platform.Foundation", "NSBundle")
    private val bundlePropertyName = "bundle"

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
            listOf(
                bundleClassName,
                ClassName("platform.objc", "object_getClass")
            )
        )
    }

    override fun getStringsPropertyInitializer(key: String): CodeBlock? {
        return CodeBlock.of("StringResource(resourceId = %S, bundle = $bundlePropertyName)", key)
    }

    override fun classMRAdditions(classSpec: TypeSpec.Builder) {
        super.classMRAdditions(classSpec)

        classSpec.addProperty(
            PropertySpec.builder(bundlePropertyName, bundleClassName, KModifier.PRIVATE)
                .initializer(CodeBlock.of("NSBundle.bundleForClass(object_getClass(this)!!)"))
                .build()
        )
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