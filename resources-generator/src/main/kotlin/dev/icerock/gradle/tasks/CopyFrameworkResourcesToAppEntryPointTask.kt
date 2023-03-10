/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.utils.propertyString
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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
            val configurationName: String =
                project.propertyString("kotlin.native.cocoapods.configuration")
                    ?: project.propertyString("moko.resources.CONFIGURATION")
                    ?: return null

            return configurationMapper[configurationName]?.name ?: configurationName
        }

    @get:Internal
    internal var configurationMapper: Map<String, NativeBuildType> = emptyMap()

    private val platformName: String?
    private val archs: String?

    init {
        group = "moko-resources"

        platformName = project.propertyString("kotlin.native.cocoapods.platform")
            ?: project.propertyString("moko.resources.PLATFORM_NAME")

        archs = project.propertyString("kotlin.native.cocoapods.archs")
            ?: project.propertyString("moko.resources.ARCHS")

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
            throw GradleException(
                "framework link task with konanTarget $konanTarget, platform $platformName, " +
                        "arch $archs and buildType $configuration not found!"
            )
        }
    }
}
