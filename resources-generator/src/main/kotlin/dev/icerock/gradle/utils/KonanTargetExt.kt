package dev.icerock.gradle.utils

import org.jetbrains.kotlin.konan.target.KonanTarget

internal fun KonanTarget.platformName(): String {
    return name.replace("ios_simulator", "iphonesimulator")
        .remove('_')
        .remove("x64")
        .remove("arm64")
}
