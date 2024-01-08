///*
// * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator.android
//
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.KModifier
//import dev.icerock.gradle.generator.ColorNode
//import dev.icerock.gradle.generator.ColorsGenerator
//import dev.icerock.gradle.generator.NOPObjectBodyExtendable
//import dev.icerock.gradle.generator.ObjectBodyExtendable
//import org.gradle.api.Project
//import org.gradle.api.file.FileTree
//import java.io.File
//
//class AndroidColorsGenerator(
//    resourcesFileTree: FileTree,
//) : ColorsGenerator(resourcesFileTree), ObjectBodyExtendable by NOPObjectBodyExtendable() {
//
//    override fun getImports() = listOf(
//        ClassName("dev.icerock.moko.graphics", "Color")
//    )
//
//    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun generateResources(
//        project: Project,
//        assetsGenerationDir: File,
//        resourcesGenerationDir: File,
//        colors: List<ColorNode>
//    ) {
//        val valuesDir = File(resourcesGenerationDir, "values")
//        val defaultStringsFile = File(valuesDir, COLORS_XML_FILE_NAME)
//        valuesDir.mkdirs()
//
//        val valuesNightDir = File(resourcesGenerationDir, "values-night")
//        val darkStringsFile = File(valuesNightDir, COLORS_XML_FILE_NAME)
//        valuesNightDir.mkdirs()
//
//        val header =
//            """
//            <?xml version="1.0" encoding="utf-8"?>
//            <resources>
//            """.trimIndent()
//
//        val footer =
//            """
//            </resources>
//            """.trimIndent()
//
//        val defaultContent = colors.joinToString("\n") { colorNode ->
//            if (colorNode.isThemed()) {
//                buildColorString(colorNode.name, replaceColorAlpha(colorNode.lightColor))
//            } else {
//                buildColorString(colorNode.name, replaceColorAlpha(colorNode.singleColor))
//            }
//        }
//
//        val darkContent = colors.filter { it.isThemed() }.joinToString("\n") { colorNode ->
//            buildColorString(colorNode.name, replaceColorAlpha(colorNode.darkColor))
//        }
//
//        defaultStringsFile.writeText(header + "\n")
//        defaultStringsFile.appendText(defaultContent)
//        defaultStringsFile.appendText("\n" + footer)
//
//        darkStringsFile.writeText(header + "\n")
//        darkStringsFile.appendText(darkContent)
//        darkStringsFile.appendText("\n" + footer)
//    }
//
//    private fun buildColorString(name: String, colorCode: String?): String {
//        return "\t<color name=\"$name\">#$colorCode</color>"
//    }
//
//    override fun getPropertyInitializer(color: ColorNode): CodeBlock {
//        return CodeBlock.of("ColorResource(resourceId = R.color.${color.name})")
//    }
//
//    companion object {
//        private const val COLORS_XML_FILE_NAME = "colors.xml"
//    }
//}
