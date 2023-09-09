/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.utils.ObservableSet

internal val KotlinCompilation<*>.allKotlinSourceSetsObservable
    get() = this.allKotlinSourceSets as ObservableSet<KotlinSourceSet>

internal val KotlinCompilation<*>.kotlinSourceSetsObservable
    get() = this.kotlinSourceSets as ObservableSet<KotlinSourceSet>
