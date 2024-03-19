/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.utils.ObservableSet

internal val KotlinSourceSet.dependsOnObservable
    get() = this.dependsOn as ObservableSet<KotlinSourceSet>
