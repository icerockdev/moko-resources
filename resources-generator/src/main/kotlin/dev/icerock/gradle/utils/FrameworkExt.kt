/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

internal val Framework.nameWithoutBuildType: String
    get() {
        val buildType: String = this.buildType.name
        val nameWithoutFramework: String = this.name.removeSuffix("Framework")

        return nameWithoutFramework.substring(
            startIndex = 0,
            endIndex = nameWithoutFramework.length - buildType.length
        )
    }
