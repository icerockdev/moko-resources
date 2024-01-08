///*
// * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator
//
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.KModifier
//import com.squareup.kotlinpoet.PropertySpec
//import com.squareup.kotlinpoet.TypeSpec
//import dev.icerock.gradle.generator.android.AndroidColorsGenerator
//import dev.icerock.gradle.generator.apple.AppleColorsGenerator
//import dev.icerock.gradle.generator.common.CommonColorsGenerator
//import dev.icerock.gradle.generator.js.JsColorsGenerator
//import dev.icerock.gradle.generator.jvm.JvmColorsGenerator
//import dev.icerock.gradle.metadata.model.GeneratedObject
//import dev.icerock.gradle.metadata.model.GeneratedObjectModifier
//import dev.icerock.gradle.metadata.model.GeneratedProperty
//import dev.icerock.gradle.metadata.model.GeneratorType
//import dev.icerock.gradle.metadata.objectsWithProperties
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.decodeFromJsonElement
//import kotlinx.serialization.json.encodeToJsonElement
//import org.gradle.api.Project
//import org.gradle.api.file.FileTree
//import org.w3c.dom.Node
//import org.w3c.dom.NodeList
//import java.io.File
//import javax.xml.parsers.DocumentBuilderFactory
//
//abstract class ColorsGenerator(
//    private val resourcesFileTree: FileTree,
//) : MRGenerator.Generator {
//
//    override val inputFiles: Iterable<File>
//        get() = resourcesFileTree.matching { it.include("**/colors*.xml") }
//    override val resourceClassName: ClassName =
//        ClassName("dev.icerock.moko.resources", "ColorResource")
//    override val mrObjectName: String = "colors"
//
//    override val type: GeneratorType = GeneratorType.Colors
//
//    open fun beforeGenerate(objectBuilder: TypeSpec.Builder, keys: List<String>) = Unit
//
//    @Suppress("SpreadOperator")
//    override fun generateObject(
//        project: Project,
//        inputMetadata: List<GeneratedObject>,
//        outputMetadata: GeneratedObject,
//        assetsGenerationDir: File,
//        resourcesGenerationDir: File,
//        objectBuilder: TypeSpec.Builder,
//    ): MRGenerator.GenerationResult? {
//        if (outputMetadata.isActual) {
//            objectBuilder.addModifiers(KModifier.ACTUAL)
//        }
//
//        if (outputMetadata.isActualObject || outputMetadata.isTargetObject) {
//            extendObjectBodyAtStart(objectBuilder)
//        }
//
//        // Read colors from previous levels, if target interface or expect
//        // return emptyList()
//        val previousColors: List<ColorNode> = getPreviousColors(
//            inputMetadata = inputMetadata,
//            targetObject = outputMetadata
//        )
//
//        // Read target colors
//        val targetColors: List<ColorNode> = parseColors()
//        val allColors: List<ColorNode> = (previousColors + targetColors).distinct()
//        val generatedProperties: MutableList<GeneratedProperty> = mutableListOf()
//
//        beforeGenerate(objectBuilder, allColors.map { it.name })
//
//        val json = Json
//
//        allColors.forEach { colorNode ->
//            val property = PropertySpec.builder(colorNode.name, resourceClassName)
//
//            // Create metadata property
//            var generatedProperty = GeneratedProperty(
//                modifier = GeneratedObjectModifier.None,
//                name = colorNode.name,
//                data = json.encodeToJsonElement(colorNode)
//            )
//
//            if (outputMetadata.isActualObject || outputMetadata.isTargetObject) {
//                // Setup property modifier and correction metadata info
//                generatedProperty = generatedProperty.copy(
//                    modifier = addActualOverrideModifier(
//                        propertyName = colorNode.name,
//                        property = property,
//                        inputMetadata = inputMetadata,
//                        targetObject = outputMetadata
//                    )
//                )
//
//                getPropertyInitializer(colorNode)?.let {
//                    property.initializer(it)
//                }
//            }
//
//            objectBuilder.addProperty(property.build())
//            generatedProperties.add(generatedProperty)
//        }
//
//        generateResources(
//            project = project,
//            assetsGenerationDir = assetsGenerationDir,
//            resourcesGenerationDir = resourcesGenerationDir,
//            colors = allColors
//        )
//
//        extendObjectBodyAtEnd(objectBuilder)
//
//        if (generatedProperties.isEmpty()) return null
//
//        return MRGenerator.GenerationResult(
//            typeSpec = objectBuilder.build(),
//            metadata = outputMetadata.copy(properties = generatedProperties)
//        )
//    }
//
//    private fun getPreviousColors(
//        inputMetadata: List<GeneratedObject>,
//        targetObject: GeneratedObject,
//    ): List<ColorNode> {
//        if (!targetObject.isObject || !targetObject.isActual) return emptyList()
//
//        val json = Json
//        val objectsWithProperties: List<GeneratedObject> = inputMetadata.objectsWithProperties(
//            targetObject = targetObject
//        )
//
//        val colors = mutableListOf<ColorNode>()
//
//        objectsWithProperties.forEach { generatedObject ->
//            generatedObject.properties.forEach { property ->
//                val colorNode = json.decodeFromJsonElement<ColorNode>(property.data)
//
//                colors.add(colorNode)
//            }
//        }
//
//        return colors
//    }
//
//    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
//
//    abstract fun getPropertyInitializer(color: ColorNode): CodeBlock?
//
//    protected open fun generateResources(
//        project: Project,
//        assetsGenerationDir: File,
//        resourcesGenerationDir: File,
//        colors: List<ColorNode>,
//    ) = Unit
//
//    private fun parseColors(): List<ColorNode> {
//        val colorNodes = mutableListOf<ColorNode>()
//        val colorValues = mutableMapOf<String, String>()
//
//        fun getColor(color: String?): String? {
//            return if (color?.startsWith(XmlColorReferencePrefix) == true) {
//                val colorName = color.replace(XmlColorReferencePrefix, "")
//                val colorValue = colorValues[colorName]
//                getColor(colorValue)
//            } else {
//                val rawColor = color?.removePrefix("#")?.removePrefix("0x")
//                if (rawColor?.length == RgbFormatLength) "${rawColor}FF" else rawColor
//            }
//        }
//
//        inputFiles.map { file ->
//            val dbFactory = DocumentBuilderFactory.newInstance()
//            val dBuilder = dbFactory.newDocumentBuilder()
//            val doc = dBuilder.parse(file)
//
//            val stringNodes = doc.getElementsByTagName(XmlColorTag)
//
//            for (i in 0 until stringNodes.length) {
//                val stringNode = stringNodes.item(i)
//
//                val colorName = stringNode.attributes.getNamedItem(XmlNodeAttrColorName).textContent
//                var lightColor: String? = null
//                var darkColor: String? = null
//                var singleColor: String? = null
//                val nodeList: NodeList = stringNode.childNodes
//                for (nodeIdx in 0 until nodeList.length) {
//                    val xmlNode: Node = nodeList.item(nodeIdx)
//                    when (xmlNode.nodeName) {
//                        "light" -> {
//                            lightColor = xmlNode.textContent
//                        }
//
//                        "dark" -> {
//                            darkColor = xmlNode.textContent
//                        }
//
//                        else -> {
//                            singleColor = xmlNode.textContent
//                            singleColor?.let {
//                                colorValues[colorName] = it
//                            }
//                        }
//                    }
//                }
//                colorNodes.add(
//                    ColorNode(
//                        colorName,
//                        getColor(lightColor),
//                        getColor(darkColor),
//                        getColor(singleColor)
//                    )
//                )
//            }
//        }
//
//        return colorNodes
//    }
//
//    class Feature(
//        private val settings: MRGenerator.Settings,
//    ) : ResourceGeneratorFeature<ColorsGenerator> {
//        override fun createCommonGenerator() = CommonColorsGenerator(
//            resourcesFileTree = settings.ownResourcesFileTree,
//        )
//
//        override fun createAppleGenerator() = AppleColorsGenerator(
//            resourcesFileTree = settings.ownResourcesFileTree,
//        )
//
//        override fun createAndroidGenerator() = AndroidColorsGenerator(
//            resourcesFileTree = settings.ownResourcesFileTree,
//        )
//
//        override fun createJsGenerator(): ColorsGenerator = JsColorsGenerator(
//            resourcesFileTree = settings.ownResourcesFileTree,
//        )
//
//        override fun createJvmGenerator() = JvmColorsGenerator(
//            resourcesFileTree = settings.ownResourcesFileTree,
//            mrClassName = settings.className
//        )
//    }
//
//    protected fun replaceColorAlpha(color: String?): String? {
//        if (color == null) return null
//
//        val alpha: String = if (isRgbFormat(color)) {
//            DefaultAlpha
//        } else {
//            color.substring(color.length - 2, color.length)
//        }
//
//        return if (isRgbFormat(color)) {
//            "$alpha$color"
//        } else {
//            "$alpha${color.removeRange(color.length - 2, color.length)}"
//        }
//    }
//
//    private fun isRgbFormat(color: String): Boolean = color.length == RgbFormatLength
//
//    companion object {
//        internal const val XmlColorTag = "color"
//        internal const val XmlNodeAttrColorName = "name"
//        internal const val XmlColorReferencePrefix = "@color/"
//        internal const val RgbFormatLength = 6
//        internal const val DefaultAlpha = "FF"
//
//        val COLORS_REGEX: Regex = "^.*/colors.*.xml".toRegex()
//    }
//}
//
//@Serializable
//data class ColorNode(
//    @SerialName("name")
//    val name: String,
//    @SerialName("lightColor")
//    val lightColor: String?, // as rgba
//    @SerialName("darkColor")
//    val darkColor: String?, // as rgba
//    @SerialName("singleColor")
//    val singleColor: String?, // as rgba
//) {
//    fun isThemed(): Boolean = lightColor != null && darkColor != null
//}
