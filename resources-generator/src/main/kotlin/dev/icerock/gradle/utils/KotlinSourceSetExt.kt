/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

@Suppress("ReturnCount")
internal fun KotlinSourceSet.isDependsOn(sourceSet: KotlinSourceSet): Boolean {
    if (dependsOn.contains(sourceSet)) return true
    dependsOn.forEach { parent ->
        if (parent.isDependsOn(sourceSet)) return true
    }
    return false
}

internal fun KotlinSourceSet.getDependedFrom(sourceSets: Collection<KotlinSourceSet>): KotlinSourceSet? {
    return sourceSets.firstOrNull { this.dependsOn.contains(it) } ?: this.dependsOn
        .mapNotNull { it.getDependedFrom(sourceSets) }
        .firstOrNull()
}
