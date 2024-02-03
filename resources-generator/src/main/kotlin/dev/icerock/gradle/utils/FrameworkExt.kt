/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

internal val Framework.nameWithoutBuildType: String
    get() {
        val buildType: String = this.buildType.name.lowercase().capitalize()

        return this.name
            .removeSuffix("Framework")
            .removeSuffix(buildType)
    }
