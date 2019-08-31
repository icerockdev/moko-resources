package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

class CommonGenerator(
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
    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.EXPECT)

    override fun getStringsPropertyInitializer(key: String): CodeBlock? = null

    override fun configureTasks(generationTask: Task, project: Project) {
        project.tasks.getByName("preBuild").dependsOn(generationTask)
        project.tasks
            .mapNotNull { it as? KotlinNativeLink }
            .forEach { it.dependsOn(generationTask) }
    }
}
