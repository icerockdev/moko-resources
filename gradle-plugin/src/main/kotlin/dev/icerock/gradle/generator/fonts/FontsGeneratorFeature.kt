/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.fonts

import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.SourceInfo

class FontsGeneratorFeature(private val info: SourceInfo) : ResourceGeneratorFeature {
    private val stringsFileTree = info.commonResources.matching {
        include("MR/fonts/**.ttf")
    }

    override fun createCommonGenerator(): MRGenerator.Generator {
        return CommonFontsGenerator(stringsFileTree)
    }

    override fun createIosGenerator(): MRGenerator.Generator {
        return IosFontsGenerator(stringsFileTree)
    }

    override fun createAndroidGenerator(): MRGenerator.Generator {
        return AndroidFontsGenerator(stringsFileTree, info.androidRClassPackage)
    }
}
