package dev.icerock.gradle.generator.plurals

import dev.icerock.gradle.ResourceGeneratorFeature
import dev.icerock.gradle.TargetInfo
import dev.icerock.gradle.generator.MRGenerator


class PluralsGeneratorFeature(private val info: TargetInfo): ResourceGeneratorFeature(info) {
    private val stringsFileTree = info.commonResources.matching{ include("MR/**/plurals.xml") }
    override fun createCommonGenerator(): MRGenerator.Generator {
        return CommonPluralsGenerator(info.sourceSet, stringsFileTree)
    }

    override fun createiOSGenerator(): MRGenerator.Generator  {
        return IosPluralsGenerator(info.sourceSet, stringsFileTree)
    }

    override fun createAndroidGenerator(): MRGenerator.Generator {
        return AndroidPluralsGenerator(info.sourceSet, stringsFileTree, info.androidRClassPackage)
    }

}