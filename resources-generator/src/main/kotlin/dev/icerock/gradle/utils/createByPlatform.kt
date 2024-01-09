/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget

internal fun <T> createByPlatform(
    kotlinPlatformType: KotlinPlatformType,
    konanTarget: () -> KonanTarget,
    createCommon: () -> T,
    createAndroid: () -> T,
    createApple: () -> T,
    createJvm: () -> T,
    createJs: () -> T,
): T {
    return when (kotlinPlatformType) {
        KotlinPlatformType.common -> createCommon()
        KotlinPlatformType.jvm -> createJvm()
        KotlinPlatformType.androidJvm -> createAndroid()
        KotlinPlatformType.js -> createJs()
        KotlinPlatformType.native -> when (konanTarget()) {
            KonanTarget.IOS_ARM32,
            KonanTarget.IOS_ARM64,
            KonanTarget.IOS_SIMULATOR_ARM64,
            KonanTarget.IOS_X64,

            KonanTarget.MACOS_ARM64,
            KonanTarget.MACOS_X64,

            KonanTarget.TVOS_ARM64,
            KonanTarget.TVOS_SIMULATOR_ARM64,
            KonanTarget.TVOS_X64,

            KonanTarget.WATCHOS_ARM32,
            KonanTarget.WATCHOS_ARM64,
            KonanTarget.WATCHOS_DEVICE_ARM64,
            KonanTarget.WATCHOS_SIMULATOR_ARM64,
            KonanTarget.WATCHOS_X64,
            KonanTarget.WATCHOS_X86 -> createApple()

            KonanTarget.ANDROID_ARM32,
            KonanTarget.ANDROID_ARM64,
            KonanTarget.ANDROID_X64,
            KonanTarget.ANDROID_X86,

            KonanTarget.LINUX_ARM32_HFP,
            KonanTarget.LINUX_ARM64,
            KonanTarget.LINUX_MIPS32,
            KonanTarget.LINUX_MIPSEL32,
            KonanTarget.LINUX_X64,

            KonanTarget.MINGW_X64,
            KonanTarget.MINGW_X86,

            KonanTarget.WASM32,

            is KonanTarget.ZEPHYR -> error("$konanTarget not supported by moko-resources now")
        }

        KotlinPlatformType.wasm -> error("$kotlinPlatformType not supported by moko-resources now")
    }
}
