/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import org.gradle.api.DefaultTask

open class GenerateMultiplatformResourcesTask : DefaultTask() {
    init {
        group = "multiplatform"
    }
}
