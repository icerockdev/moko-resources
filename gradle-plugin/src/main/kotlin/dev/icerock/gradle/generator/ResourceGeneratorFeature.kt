/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

interface ResourceGeneratorFeature {
    fun createCommonGenerator(): MRGenerator.Generator
    fun createIosGenerator(): MRGenerator.Generator
    fun createAndroidGenerator(): MRGenerator.Generator
}
