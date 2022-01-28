/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

@Deprecated("Please use original FatFrameworkTask from kotlin plugin")
open class FatFrameworkWithResourcesTask : FatFrameworkTask()
