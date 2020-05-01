/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.image

import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.SourceInfo

class ImagesGeneratorFeature(private val info: SourceInfo) : ResourceGeneratorFeature {
    private val stringsFileTree = info.commonResources.matching {
        include("MR/images/**/*.png", "MR/images/**/*.jpg")
    }

    override fun createCommonGenerator(): MRGenerator.Generator {
        return CommonImagesGenerator(stringsFileTree)
    }

    override fun createIosGenerator(): MRGenerator.Generator {
        return IosImagesGenerator(stringsFileTree)
    }

    override fun createAndroidGenerator(): MRGenerator.Generator {
        return AndroidImagesGenerator(stringsFileTree, info.androidRClassPackage)
    }
}
