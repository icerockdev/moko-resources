/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.utils.ObservableSet
import java.util.concurrent.atomic.AtomicBoolean

internal val KotlinSourceSet.dependsOnObservable
    get() = this.dependsOn as ObservableSet<KotlinSourceSet>

@Suppress("ReturnCount")
internal fun KotlinSourceSet.isDependsOn(sourceSet: KotlinSourceSet): Boolean {
    if (dependsOn.contains(sourceSet)) return true
    dependsOn.forEach { parent ->
        if (parent.isDependsOn(sourceSet)) return true
    }
    return false
}

internal fun KotlinSourceSet.ifDependsOn(
    sourceSet: KotlinSourceSet,
    block: () -> Unit
) = ifDependsOn(sourceSet, AtomicBoolean(false), block)

private fun KotlinSourceSet.ifDependsOn(
    sourceSet: KotlinSourceSet,
    sourceSetFound: AtomicBoolean,
    block: () -> Unit
) {
    if (this == sourceSet) {
        if (sourceSetFound.compareAndSet(false, true)) {
            block()
        }
    } else {
        dependsOnObservable.forAll { parent ->
            parent.ifDependsOn(sourceSet, sourceSetFound, block)
        }
    }
}
