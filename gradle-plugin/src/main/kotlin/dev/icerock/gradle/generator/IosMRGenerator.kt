/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.support.unzipTo
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.file.zipDirAs
import org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import java.io.FileWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class IosMRGenerator(
    generatedDir: File,
    sourceSet: SourceSet,
    mrClassPackage: String,
    private val generators: List<Generator>,
    private val compilation: AbstractKotlinNativeCompilation
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrClassPackage = mrClassPackage,
    generators = generators
) {
    private val bundleClassName =
        ClassName("platform.Foundation", "NSBundle")

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun processMRClass(mrClass: TypeSpec.Builder) {
        super.processMRClass(mrClass)

        mrClass.addProperty(
            PropertySpec.builder(
                BUNDLE_PROPERTY_NAME,
                bundleClassName,
                KModifier.PRIVATE
            )
                .initializer(CodeBlock.of("NSBundle.bundleForClass(object_getClass(this)!!)"))
                .build()
        )
    }

    override fun getImports(): List<ClassName> = listOf(
        bundleClassName,
        ClassName("platform.objc", "object_getClass")
    )

    override fun apply(generationTask: Task, project: Project) {
        setupKLibResources(generationTask)
        setupFrameworkResources()
    }

    private fun setupKLibResources(generationTask: Task) {
        val compileTask: KotlinNativeCompile = compilation.compileKotlinTask
        compileTask.dependsOn(generationTask)

        compileTask.doLast {
            this as KotlinNativeCompile

            val klibFile = this.outputFile.get()
            val repackDir = File(klibFile.parent, klibFile.nameWithoutExtension)
            val resRepackDir = File(repackDir, "resources")

            unzipTo(zipFile = klibFile, outputDirectory = repackDir)

            resourcesGenerationDir.copyRecursively(resRepackDir, overwrite = true)

            val repackKonan = org.jetbrains.kotlin.konan.file.File(repackDir.path)
            val klibKonan = org.jetbrains.kotlin.konan.file.File(klibFile.path)

            repackKonan.zipDirAs(klibKonan)

            repackDir.deleteRecursively()
        }
    }

    private fun setupFrameworkResources() {
        val kotlinNativeTarget = compilation.target as KotlinNativeTarget

        val frameworkBinaries: List<Framework> = kotlinNativeTarget.binaries
            .filterIsInstance<Framework>()
            .filter { it.compilation == compilation }

        frameworkBinaries.forEach { framework ->
            val linkTask = framework.linkTask

            linkTask.doLast {
                linkTask.libraries
                    .plus(linkTask.intermediateLibrary.get())
                    .filter { it.extension == "klib" }
                    .forEach {
                        project.logger.warn("copy resources from $it")
                        val klibKonan = org.jetbrains.kotlin.konan.file.File(it.path)
                        val klib = KotlinLibraryLayoutImpl(klibKonan)
                        val layout = klib.extractingToTemp
                        File(layout.resourcesDir.path).copyRecursively(framework.outputFile, overwrite = true)
                    }

                processInfoPlist(framework)
            }
        }
    }

    private fun processInfoPlist(framework: Framework) {
        val infoPList = File(framework.outputFile, "Info.plist")

        val dbFactory = DocumentBuilderFactory.newInstance()
        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(infoPList)

        val rootDict = doc.getElementsByTagName("dict").item(0)

        generators
            .mapNotNull { it as? ExtendsPlistDictionary }
            .forEach { it.appendPlistInfo(doc, rootDict) }

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")

        val writer = FileWriter(infoPList)
        val result = StreamResult(writer)

        transformer.transform(DOMSource(doc), result)
    }

    companion object {
        const val BUNDLE_PROPERTY_NAME = "bundle"
    }
}

interface ExtendsPlistDictionary {
    fun appendPlistInfo(doc: Document, rootDict: Node)
}
