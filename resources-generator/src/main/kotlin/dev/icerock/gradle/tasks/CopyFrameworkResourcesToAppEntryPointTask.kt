/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.KonanTarget

open class CopyFrameworkResourcesToAppEntryPointTask : DefaultTask() {

    @get:Internal
    internal val konanTarget: KonanTarget?
    @get:Internal
    internal val configuration: String? get() {
        val configurationName =
            (project.findProperty("moko.resources.CONFIGURATION") as? String)

        return configurationMapper[configurationName]?.name ?: configurationName
    }
    @get:Internal
    internal val platformName: String?
    @get:Internal
    internal var configurationMapper: Map<String, NativeBuildType> = emptyMap()

    init {
        group = "moko-resources"

        platformName =
            project.findProperty("moko.resources.PLATFORM_NAME") as? String

        konanTarget = when (platformName) {
            "iphonesimulator" -> KonanTarget.IOS_X64
            "iphoneos" -> KonanTarget.IOS_ARM64
            "macosx" -> KonanTarget.MACOS_X64
            else -> null
        }
    }

    @TaskAction
    fun action() {
        if (dependsOn.isEmpty()) {
            throw IllegalStateException(
                """
framework link task with konanTarget $konanTarget, platform $platformName and buildType $configuration not found!
"""
            )
        }
    }
}
