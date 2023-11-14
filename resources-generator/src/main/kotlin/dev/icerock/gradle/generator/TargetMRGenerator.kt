package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.toModifier
import org.gradle.api.Project

abstract class TargetMRGenerator(
    settings: Settings,
    generators: List<Generator>,
) : MRGenerator(
    settings = settings,
    generators = generators
) {
    override fun generateFileSpec(): FileSpec? {
        val visibilityModifier: KModifier = settings.visibility.toModifier()

        @Suppress("SpreadOperator")
        val mrClassSpec = TypeSpec.objectBuilder(settings.className)
            .addModifiers(*getMRClassModifiers())
            .addModifiers(visibilityModifier)

        generators.forEach { generator ->
            val builder = TypeSpec.objectBuilder(generator.mrObjectName)
                .addModifiers(visibilityModifier)

//            val fileResourceInterfaceClassName = ClassName(
//                packageName = "dev.icerock.moko.resources",
//                "ResourceContainer"
//            )
//            builder.addSuperinterface(
//                fileResourceInterfaceClassName.parameterizedBy(generator.resourceClassName)
//            )

            mrClassSpec.addType(
                generator.generate(
                    assetsGenerationDir,
                    resourcesGenerationDir,
                    builder
                )
            )
        }

        processMRClass(mrClassSpec)

        val mrClass = mrClassSpec.build()

        val fileSpec = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        ).addType(mrClass)

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        return fileSpec.build()
    }

    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {

    }

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

}