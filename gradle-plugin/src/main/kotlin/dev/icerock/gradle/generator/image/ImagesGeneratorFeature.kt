package dev.icerock.gradle.generator.image

import dev.icerock.gradle.ResourceGeneratorFeature
import dev.icerock.gradle.TargetInfo
import dev.icerock.gradle.generator.MRGenerator


class ImagesGeneratorFeature(private val info: TargetInfo): ResourceGeneratorFeature(info) {
    private val stringsFileTree = info.commonResources.matching{
        include("MR/images/**/*.png", "MR/images/**/*.jpg")
    }

    override fun createCommonGenerator(): MRGenerator.Generator {
        return CommonImagesGenerator(info.sourceSet, stringsFileTree)
    }

    override fun createiOSGenerator(): MRGenerator.Generator  {
        return IosImagesGenerator(info.sourceSet, stringsFileTree)
    }

    override fun createAndroidGenerator(): MRGenerator.Generator {
        return AndroidImagesGenerator(info.sourceSet, stringsFileTree, info.androidRClassPackage)
    }

}