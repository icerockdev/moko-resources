/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
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
    generators: List<Generator>
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
            linkTask.dependsOn(generationTask)

            val framework = linkTask.binary as? Framework ?: return@forEach

            val pack =
                project.tasks.create(linkTask.name.replace("link", "pack"), Copy::class.java) {
                    group = "multiplatform"

                    from(resourcesGenerationDir)
                    into(framework.outputFile)

                    // set development region in Info.plist for activate Base.lproj usage
                    doLast {
                        val infoPList = File(framework.outputFile, "Info.plist")

                        val dbFactory = DocumentBuilderFactory.newInstance()
                        val dBuilder = dbFactory.newDocumentBuilder()
                        val doc = dBuilder.parse(infoPList)

                        val rootDict = doc.getElementsByTagName("dict").item(0)

                        rootDict.appendChild(doc.createElement("key").apply {
                            textContent = "CFBundleDevelopmentRegion"
                        })
                        rootDict.appendChild(doc.createElement("string").apply {
                            textContent = "en"
                        })

                        val transformerFactory = TransformerFactory.newInstance()
                        val transformer = transformerFactory.newTransformer()
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                        val writer = FileWriter(infoPList)
                        val result = StreamResult(writer)

                        transformer.transform(DOMSource(doc), result)
                    }
                }

            linkTask.finalizedBy(pack)
        }
    }

    companion object {
        const val BUNDLE_PROPERTY_NAME = "bundle"
    }
}