/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.strings

import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.SourceInfo

class StringsGeneratorFeature(
    private val info: SourceInfo,
    private val iosBaseLocalizationRegion: String
) : ResourceGeneratorFeature {
    private val stringsFileTree = info.commonResources.matching { include("MR/**/strings.xml") }
    override fun createCommonGenerator(): MRGenerator.Generator {
        return CommonStringsGenerator(stringsFileTree)
    }

    override fun createIosGenerator(): MRGenerator.Generator {
        return IosStringsGenerator(stringsFileTree, iosBaseLocalizationRegion)
    }

    override fun createAndroidGenerator(): MRGenerator.Generator {
        return AndroidStringsGenerator(stringsFileTree, info.androidRClassPackage)
    }
}
