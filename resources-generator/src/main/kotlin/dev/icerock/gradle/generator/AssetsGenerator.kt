package dev.icerock.gradle.generator

import dev.icerock.gradle.generator.android.AndroidAssetsGenerator
import dev.icerock.gradle.generator.apple.AppleAssetsGenerator
import dev.icerock.gradle.generator.common.CommonAssetsGenerator
import org.gradle.api.file.FileTree
import java.io.File

abstract class AssetsGenerator(
    val inputFile: FileTree
) : MRGenerator.GeneratorWithoutClass {

    override val inputFiles: Iterable<File> = inputFile.files

    class Feature(info: SourceInfo) : ResourceGeneratorFeature<AssetsGenerator> {

        val assetsFileTree = info.commonResources.matching {
            it.include("assets/**")
        }

        override fun createCommonGenerator(): AssetsGenerator {
            return CommonAssetsGenerator(assetsFileTree)
        }

        override fun createIosGenerator(): AssetsGenerator {
            return AppleAssetsGenerator(assetsFileTree)
        }

        override fun createAndroidGenerator(): AssetsGenerator {
            return AndroidAssetsGenerator(assetsFileTree)
        }
    }
}
