/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

/*
This code ensures that the Bundle in an iOS application, built with Kotlin Multiplatform (KMP), can be correctly
located at runtime. The issue arises because Kotlin doesn’t allow direct lookup of a Bundle by a class from
Objective-C. To resolve this, a static library written in Objective-C was created and automatically included in the
Kotlin Framework during the build process. This library contains a class used to locate the required Bundle.

Key steps performed by the code:

1. Handling Apple targets in KMP:
  The code automatically configures the build for Apple platforms only (iOS, macOS, tvOS, watchOS).
2. Compiling and linking the static library:
  - clang is used to compile the source file MRResourcesBundle.m into an object file.
  - The object file is linked into a static library (libMRResourcesBundle.a) using the ar utility.
3. Integrating the static library into the Kotlin Framework:
  - A C-interop is created, enabling Kotlin to interact with the Objective-C code from the library.
  - The C-interop task is configured to depend on the compilation and linking tasks, ensuring the library is ready for
  use during the build process.
4. Support for multiple Apple platforms:
  - The code adapts the build process for specific Apple SDKs and architectures by using helper functions getAppleSdk
  and getClangTarget.
5. Retrieving the SDK path:
  The xcrun utility is used to dynamically fetch the SDK path required by clang.

What does this achieve?

As a result, a Kotlin Multiplatform application for iOS, macOS, tvOS, or watchOS can correctly locate the Bundle
containing resources by leveraging standard Apple APIs wrapped in the static library. This process is fully automated
during the project build, requiring no manual intervention from the developer.

Bundle search logic:
resources/src/appleMain/kotlin/dev/icerock/moko/resources/utils/NSBundleExt.kt
*/

kotlin.targets
    .withType<KotlinNativeTarget>()
    .matching { it.konanTarget.family.isAppleFamily }
    .configureEach {
        val sdk: String = this.konanTarget.getAppleSdk()
        val target: String = this.konanTarget.getClangTarget()

        val sdkPath: String = getSdkPath(sdk)

        val libsDir = File(buildDir, "moko-resources/cinterop/$name")
        libsDir.mkdirs()
        val sourceFile = File(projectDir, "src/appleMain/objective-c/MRResourcesBundle.m")
        val objectFile = File(libsDir, "MRResourcesBundle.o")
        val libFile = File(libsDir, "libMRResourcesBundle.a")
        val kotlinTargetPostfix: String = this.name.capitalize()

        val compileStaticLibrary = tasks.register("mokoBundleSearcherCompile$kotlinTargetPostfix", Exec::class) {
            group = "moko-resources"

            commandLine = listOf(
                "clang",
                "-target",
                target,
                "-isysroot",
                sdkPath,
                "-c",
                sourceFile.absolutePath,
                "-o",
                objectFile.absolutePath
            )
            outputs.file(objectFile.absolutePath)
        }
        val linkStaticLibrary = tasks.register("mokoBundleSearcherLink$kotlinTargetPostfix", Exec::class) {
            group = "moko-resources"

            dependsOn(compileStaticLibrary)

            commandLine = listOf(
                "ar",
                "rcs",
                libFile.absolutePath,
                objectFile.absolutePath
            )
            outputs.file(libFile.absolutePath)
        }

        compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
            val bundleSearcher by cinterops.creating {
                defFile(project.file("src/appleMain/def/bundleSearcher.def"))

                includeDirs("$projectDir/src/appleMain/objective-c")
                extraOpts("-libraryPath", libsDir.absolutePath)
            }

            tasks.named(bundleSearcher.interopProcessingTaskName).configure {
                dependsOn(linkStaticLibrary)
            }
        }
    }

fun KonanTarget.getAppleSdk(): String {
    return when (this) {
        KonanTarget.IOS_ARM32,
        KonanTarget.IOS_ARM64 -> "iphoneos"

        KonanTarget.IOS_SIMULATOR_ARM64,
        KonanTarget.IOS_X64 -> "iphonesimulator"

        KonanTarget.MACOS_ARM64,
        KonanTarget.MACOS_X64 -> "macosx"

        KonanTarget.TVOS_ARM64 -> "appletvos"

        KonanTarget.TVOS_SIMULATOR_ARM64,
        KonanTarget.TVOS_X64 -> "appletvsimulator"

        KonanTarget.WATCHOS_ARM32,
        KonanTarget.WATCHOS_DEVICE_ARM64 -> "watchos"

        KonanTarget.WATCHOS_ARM64,
        KonanTarget.WATCHOS_SIMULATOR_ARM64,
        KonanTarget.WATCHOS_X64,
        KonanTarget.WATCHOS_X86 -> "watchsimulator"

        else -> error("Unsupported target for selecting SDK: $this")
    }
}

fun KonanTarget.getClangTarget(): String {
    return when (this) {
        KonanTarget.IOS_ARM32 -> "armv7-apple-ios"
        KonanTarget.IOS_ARM64 -> "arm64-apple-ios"
        KonanTarget.IOS_SIMULATOR_ARM64 -> "arm64-apple-ios-simulator"
        KonanTarget.IOS_X64 -> "x86_64-apple-ios-simulator"

        KonanTarget.MACOS_ARM64 -> "arm64-apple-macosx"
        KonanTarget.MACOS_X64 -> "x86_64-apple-macosx"

        KonanTarget.TVOS_ARM64 -> "arm64-apple-tvos"
        KonanTarget.TVOS_SIMULATOR_ARM64 -> "arm64-apple-tvsimulator"
        KonanTarget.TVOS_X64 -> "x86_64-apple-tvsimulator"

        KonanTarget.WATCHOS_ARM32 -> "armv7k-apple-watchos"
        KonanTarget.WATCHOS_ARM64 -> "arm64-apple-watchos"
        KonanTarget.WATCHOS_DEVICE_ARM64 -> "arm64_32-apple-watchos"
        KonanTarget.WATCHOS_SIMULATOR_ARM64 -> "arm64-apple-watchos"
        KonanTarget.WATCHOS_X64 -> "x86_64-apple-watchos"
        KonanTarget.WATCHOS_X86 -> "i386-apple-watchos"

        else -> error("Unsupported target for selecting clang target: $this")
    }
}

fun getSdkPath(sdk: String): String {
    val process = ProcessBuilder("xcrun", "--sdk", sdk, "--show-sdk-path")
        .redirectErrorStream(true)
        .start()
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}
