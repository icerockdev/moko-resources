/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.fonts

import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.SourceInfo


class FontsGeneratorFeature(private val info: SourceInfo) : ResourceGeneratorFeature(info) {
    private val stringsFileTree = info.commonResources.matching {
        include("MR/fonts/**.ttf")
    }

    override fun createCommonGenerator(): MRGenerator.Generator {
        return CommonFontsGenerator(info.sourceSet, stringsFileTree)
    }

    override fun createiOSGenerator(): MRGenerator.Generator {
        return IosFontsGenerator(info.sourceSet, stringsFileTree)
    }

    override fun createAndroidGenerator(): MRGenerator.Generator {
        return AndroidFontsGenerator(info.sourceSet, stringsFileTree, info.androidRClassPackage)
    }

}