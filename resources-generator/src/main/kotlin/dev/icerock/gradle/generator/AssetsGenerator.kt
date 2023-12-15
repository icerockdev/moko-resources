/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidAssetsGenerator
import dev.icerock.gradle.generator.apple.AppleAssetsGenerator
import dev.icerock.gradle.generator.common.CommonAssetsGenerator
import dev.icerock.gradle.generator.js.JsAssetsGenerator
import dev.icerock.gradle.generator.jvm.JvmAssetsGenerator
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratorType
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import java.io.File

@Suppress("TooManyFunctions")
abstract class AssetsGenerator(
    private val fileTree: FileTree
) : MRGenerator.Generator {
    override val inputFiles: Iterable<File>
        get() = fileTree.files
    override val mrObjectName: String = ASSETS_DIR_NAME
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "AssetResource")

    override val type: GeneratorType = GeneratorType.Assets

    private fun getBaseDir(file: File): String {
        val relativePathToAssets = file.path.substringAfterLast(ASSETS_DIR_NAME)
        val fixedRelativePath = File(relativePathToAssets).path

        val result: String = if (fixedRelativePath.startsWith(File.separatorChar)) {
            fixedRelativePath.substring(1)
        } else {
            fixedRelativePath
        }

        return if (File.separatorChar == '/') result else result.replace(File.separatorChar, '/')
    }

    private fun parseRootContentInner(folders: Array<File>): List<AssetSpec> {
        val res = mutableListOf<AssetSpec>()
        for (it in folders) {
            if (it.isDirectory) {
                val files = it.listFiles()
                if (!files.isNullOrEmpty()) {
                    res.add(AssetSpecDirectory(it.name, parseRootContentInner(files)))
                }
            } else {
                // skip empty files, like .DS_Store
                if (it.nameWithoutExtension.isEmpty()) {
                    continue
                }

                val pathRelativeToBase = getBaseDir(it)

                if (pathRelativeToBase.contains(PATH_DELIMITER)) {
                    error("file path can't have this symbol $PATH_DELIMITER. We use them as separators.")
                }

                res.add(
                    AssetSpecFile(
                        pathRelativeToBase = pathRelativeToBase,
                        file = it
                    )
                )
            }
        }
        return res
    }

    private fun parseRootContent(
        resFolders: Set<File>
    ): List<AssetSpec> {
        val contentOfRootDir = mutableListOf<File>()
        resFolders.forEach {
            val assets = File(it, ASSETS_DIR_NAME)

            val content = assets.listFiles()
            if (content != null) {
                contentOfRootDir.addAll(content)
            }
        }
        return parseRootContentInner(contentOfRootDir.toTypedArray())
    }

    override fun generate(
        project: Project,
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        val rootContent = parseRootContent(fileTree.files)

        beforeGenerate(objectBuilder, rootContent)

        val typeSpec = createTypeSpec(rootContent, objectBuilder)

        generateResources(assetsGenerationDir, resourcesGenerationDir, rootContent)

        return typeSpec
    }

    private fun createTypeSpec(keys: List<AssetSpec>, objectBuilder: TypeSpec.Builder): TypeSpec {
        @Suppress("SpreadOperator")
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        createInnerTypeSpec(keys, objectBuilder)

        extendObjectBodyAtEnd(objectBuilder)

        return objectBuilder.build()
    }

    @Suppress("SpreadOperator")
    private fun createInnerTypeSpec(keys: List<AssetSpec>, objectBuilder: TypeSpec.Builder) {
        for (specs in keys) {
            if (specs is AssetSpecFile) {
                val styleProperty = PropertySpec
                    .builder(specs.file.nameWithoutExtension.replace('-', '_'), resourceClassName)
                    .addModifiers(*getPropertyModifiers())

                getPropertyInitializer(specs)?.let { codeBlock ->
                    styleProperty.initializer(codeBlock)
                }
                objectBuilder.addProperty(styleProperty.build())
            } else if (specs is AssetSpecDirectory) {
                val spec = TypeSpec
                    .objectBuilder(specs.name.replace('-', '_'))
                    .addModifiers(*getClassModifiers())

                createInnerTypeSpec(specs.assets, spec)

                objectBuilder.addType(spec.build())
            }
        }
    }

    override fun getImports(): List<ClassName> = emptyList()

    protected open fun beforeGenerate(
        objectBuilder: TypeSpec.Builder,
        files: List<AssetSpec>
    ) {
    }

    protected open fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<AssetSpec>
    ) {
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fileSpec: AssetSpecFile): CodeBlock?


    sealed class AssetSpec

    class AssetSpecDirectory(val name: String, val assets: List<AssetSpec>) : AssetSpec()

    /**
     * @param pathRelativeToBase used to copy necessary resources in AssetsGenerator
     * @param file is a new name a of copied resource for systems which do not support path with / symbol
     */
    class AssetSpecFile(
        val pathRelativeToBase: String,
        val file: File
    ) : AssetSpec()

    class Feature(
        private val settings: MRGenerator.Settings
    ) : ResourceGeneratorFeature<AssetsGenerator> {

        override fun createCommonGenerator(): AssetsGenerator = CommonAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            upperResourcesFileTree = settings.upperResourcesFileTree,
        )

        override fun createAppleGenerator(): AssetsGenerator = AppleAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            lowerResourcesFileTree = settings.lowerResourcesFileTree,
        )

        override fun createAndroidGenerator(): AssetsGenerator = AndroidAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            lowerResourcesFileTree = settings.lowerResourcesFileTree,
        )

        override fun createJvmGenerator(): AssetsGenerator = JvmAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            settings = settings
        )

        override fun createJsGenerator(): AssetsGenerator = JsAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
        )
    }

    companion object {
        const val ASSETS_DIR_NAME: String = "assets"

        /*
        This is used for property name in MR class as well as a replacement of / for platforms which
        don't support it like apple.
        */
        const val PATH_DELIMITER = '+'
    }
}
