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
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
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
    sourceSet: KotlinSourceSet,
    mrClassPackage: String,
    private val generators: List<Generator>
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
        val linkTasks = project.tasks
            .mapNotNull { it as? KotlinNativeLink }
            .filter { it.binary is Framework }
            .filter { it.compilation.kotlinSourceSets.contains(sourceSet) }

        linkTasks.forEach { linkTask ->
            linkTask.compilation.compileKotlinTask.dependsOn(generationTask)

            val framework = linkTask.binary as? Framework ?: return@forEach

            linkTask.doLast {
                resourcesGenerationDir.copyRecursively(framework.outputFile, overwrite = true)

                val infoPList = File(framework.outputFile, "Info.plist")

                val dbFactory = DocumentBuilderFactory.newInstance()
                dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                val dBuilder = dbFactory.newDocumentBuilder()
                val doc = dBuilder.parse(infoPList)

                val rootDict = doc.getElementsByTagName("dict").item(0)

                generators.forEach { generator ->
                    (generator as? ExtendsPlistDictionary)?.let {
                        it.appendPlistInfo(doc, rootDict)
                    }
                }

                val transformerFactory = TransformerFactory.newInstance()
                val transformer = transformerFactory.newTransformer()
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")

                val writer = FileWriter(infoPList)
                val result = StreamResult(writer)

                transformer.transform(DOMSource(doc), result)
            }
        }
    }

    companion object {
        const val BUNDLE_PROPERTY_NAME = "bundle"
    }
}

interface ExtendsPlistDictionary {
    fun appendPlistInfo(doc: Document, rootDict: Node)
}
