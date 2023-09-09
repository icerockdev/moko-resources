/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider

interface SourceInfo {
    val commonResources: SourceDirectorySet
    val mrClassPackage: Provider<String>
    var androidRClassPackageProvider: Provider<String>?
}
