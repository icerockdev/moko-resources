package dev.icerock.gradle.utils

import org.gradle.api.GradleException
import org.jetbrains.kotlin.konan.target.KonanTarget

internal fun getKonanTargets(
    platform: String,
    arch: List<String>,
): List<KonanTarget> {
    return arch.map {
        getKonanTarget(platform, it)
    }
}

@Suppress("CyclomaticComplexMethod", "ThrowsCount")
internal fun getKonanTarget(
    platform: String,
    arch: String,
): KonanTarget {
    return when {
        platform.startsWith("iphoneos") -> {
            when (arch) {
                "arm64", "arm64e" -> KonanTarget.IOS_ARM64
                "armv7", "armv7s" -> KonanTarget.IOS_ARM32
                else -> throw GradleException("UnknownArchitectureException: $platform, $arch")
            }
        }
        platform.startsWith("iphonesimulator") -> {
            when (arch) {
                "arm64", "arm64e" -> KonanTarget.IOS_SIMULATOR_ARM64
                "x86_64" -> KonanTarget.IOS_X64
                else -> throw GradleException("UnknownArchitectureException: $platform, $arch")
            }
        }
        platform.startsWith("watchos") -> {
            when (arch) {
                "armv7k" -> KonanTarget.WATCHOS_ARM32
                "arm64_32" -> KonanTarget.WATCHOS_ARM64
                "arm64" -> KonanTarget.WATCHOS_DEVICE_ARM64
                else -> throw GradleException("UnknownArchitectureException: $platform, $arch")
            }
        }
        platform.startsWith("watchsimulator") -> {
            when (arch) {
                "arm64", "arm64e" -> KonanTarget.WATCHOS_SIMULATOR_ARM64
                "i386" -> KonanTarget.WATCHOS_X86
                "x86_64" -> KonanTarget.WATCHOS_X64
                else -> throw GradleException("UnknownArchitectureException: $platform, $arch")
            }
        }
        platform.startsWith("appletvos") -> {
            when (arch) {
                "arm64", "arm64e" -> KonanTarget.TVOS_ARM64
                else -> throw GradleException("UnknownArchitectureException: $platform, $arch")
            }
        }
        platform.startsWith("appletvsimulator") -> {
            when (arch) {
                "arm64", "arm64e" -> KonanTarget.TVOS_SIMULATOR_ARM64
                "x86_64" -> KonanTarget.TVOS_X64
                else -> throw GradleException("UnknownArchitectureException: $platform, $arch")
            }
        }
        platform.startsWith("macosx") -> {
            when (arch) {
                "arm64" -> KonanTarget.MACOS_ARM64
                "x86_64" -> KonanTarget.MACOS_X64
                else -> throw GradleException("UnknownArchitectureException: $platform, $arch")
            }
        }
        else -> throw IllegalArgumentException("Platform $platform is not supported")
    }
}
