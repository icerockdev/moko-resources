/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

abstract class ResourceGeneratorFeature(info: SourceInfo) {
    abstract fun createCommonGenerator(): MRGenerator.Generator
    abstract fun createiOSGenerator(): MRGenerator.Generator
    abstract fun createAndroidGenerator(): MRGenerator.Generator
}