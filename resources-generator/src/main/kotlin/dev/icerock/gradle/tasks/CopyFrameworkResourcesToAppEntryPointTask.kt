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
    internal val configuration: String?
        get() {
            val configurationName =
                (project.findProperty("moko.resources.CONFIGURATION") as? String)

            return configurationMapper[configurationName]?.name ?: configurationName
        }

    @get:Internal
    internal val platformName: String?

    @get:Internal
    internal val archs: String?

    @get:Internal
    internal var configurationMapper: Map<String, NativeBuildType> = emptyMap()

    init {
        group = "moko-resources"

        platformName =
            project.findProperty("moko.resources.PLATFORM_NAME") as? String

        archs = project.findProperty("kotlin.native.cocoapods.archs") as? String

        konanTarget = when (platformName) {
            "iphonesimulator" -> when {
                archs?.contains("arm64") == true -> KonanTarget.IOS_SIMULATOR_ARM64
                archs?.contains("x86_64") == true -> KonanTarget.IOS_X64
                else -> null
            }
            "iphoneos" -> KonanTarget.IOS_ARM64
            "macosx" -> when {
                archs?.contains("arm64") == true -> KonanTarget.MACOS_ARM64
                archs?.contains("x86_64") == true -> KonanTarget.MACOS_X64
                else -> null
            }
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
