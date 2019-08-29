/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class MultiplatformResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val multiplatformExtension =
            target.extensions.getByType(KotlinMultiplatformExtension::class)
        val commonMain =
            multiplatformExtension.sourceSets.getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
        val commonResources = commonMain.resources

        val strings = commonResources.matching {
            include("MR/**/strings.xml")
        }

        val generatedDir = File(target.buildDir, "generated/moko")
        val commonGeneratedDir = File(generatedDir, "commonMain")

        commonMain.kotlin.srcDir(commonGeneratedDir)

        val baseStrings = generateStrings(target, strings)

        val mrClass = TypeSpec.objectBuilder("MR")
            .addModifiers(KModifier.EXPECT)
            .addType(baseStrings)
            .build()

        val file = FileSpec.builder("dev.icerock.mobile", "MR")
            .addType(mrClass)
            .build()
        file.writeTo(commonGeneratedDir)
    }

    private fun generateStrings(target: Project, stringsFileTree: FileTree): TypeSpec {
        val baseStringsFile = stringsFileTree.single { it.parentFile.name == "base" }

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(baseStringsFile)

        val stringNodes = doc.getElementsByTagName("string")
        val classBuilder = TypeSpec.objectBuilder("strings")

        val stringResourceClass = ClassName("dev.icerock.moko.resources", "StringResource")

        for (i in 0 until stringNodes.length) {
            val stringNode = stringNodes.item(i)
            val name = stringNode.attributes.getNamedItem("name").textContent

            classBuilder.addProperty(name, stringResourceClass)
        }

        return classBuilder.build()
    }
}
