package dev.icerock.gradle.generator.strings

import dev.icerock.gradle.ResourceGeneratorFeature
import dev.icerock.gradle.TargetInfo
import dev.icerock.gradle.generator.MRGenerator

class StringsGeneratorFeature(private val info: TargetInfo): ResourceGeneratorFeature(info) {
    private val stringsFileTree = info.commonResources.matching{ include("MR/**/strings.xml") }
    override fun createCommonGenerator(): MRGenerator.Generator {
        return CommonStringsGenerator(info.sourceSet, stringsFileTree)
    }

    override fun createiOSGenerator(): MRGenerator.Generator  {
        return IosStringsGenerator(info.sourceSet, stringsFileTree)
    }

    override fun createAndroidGenerator(): MRGenerator.Generator {
        return AndroidStringsGenerator(info.sourceSet, stringsFileTree, info.androidRClassPackage)
    }

}