import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File
import kotlin.reflect.full.declaredMemberProperties

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("dev.icerock.mobile.multiplatform-resources")
}

version = "0.1.0"

kotlin {
    macosX64 {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal"
                )
            }
        }
    }

    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal"
                )
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":shared"))
            }
        }
    }
}

compose.desktop.nativeApplication {
    targets(kotlin.targets.getByName("macosX64"), kotlin.targets.getByName("macosArm64"))
    distributions {
        targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg)
        packageName = "dev.icerock.moko.resources.sample"
        packageVersion = "0.1.0"
    }
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            // TODO: the current compose binary surprises LLVM, so disable checks for now.
            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "dev.icerock.moko.resources.sample"
}

// TODO move to moko-resources gradle plugin
// copy .bundle from all .klib to .kexe
tasks.withType<KotlinNativeLink>()
    .configureEach {
        val linkTask: KotlinNativeLink = this
        val outputDir: File = this.outputFile.get().parentFile

        @Suppress("ObjectLiteralToLambda") // lambda broke up-to-date
        val action = object : Action<Task> {
            override fun execute(t: Task) {
                (linkTask.libraries + linkTask.sources)
                    .filter { library -> library.extension == "klib" }
                    .filter(File::exists)
                    .forEach { inputFile ->
                        val klibKonan = org.jetbrains.kotlin.konan.file.File(inputFile.path)
                        val klib = org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl(
                            klib = klibKonan,
                            component = "default"
                        )
                        val layout = klib.extractingToTemp

                        // extracting bundles
                        layout
                            .resourcesDir
                            .absolutePath
                            .let(::File)
                            .listFiles { file: File -> file.extension == "bundle" }
                            // copying bundles to app
                            ?.forEach {
                                logger.info("${it.absolutePath} copying to $outputDir")
                                it.copyRecursively(
                                    target = File(outputDir, it.name),
                                    overwrite = true
                                )
                            }
                    }
            }
        }
        doLast(action)
    }

// TODO move to moko-resources gradle plugin
// copy .bundle from .kexe to .app
tasks.withType<org.jetbrains.compose.experimental.uikit.tasks.ExperimentalPackComposeApplicationForXCodeTask>()
    .configureEach {
        val packTask: org.jetbrains.compose.experimental.uikit.tasks.ExperimentalPackComposeApplicationForXCodeTask =
            this

        val kclass =
            org.jetbrains.compose.experimental.uikit.tasks.ExperimentalPackComposeApplicationForXCodeTask::class
        val kotlinBinaryField =
            kclass.declaredMemberProperties.single { it.name == "kotlinBinary" }
        val destinationDirField =
            kclass.declaredMemberProperties.single { it.name == "destinationDir" }
        val executablePathField =
            kclass.declaredMemberProperties.single { it.name == "executablePath" }

        @Suppress("ObjectLiteralToLambda") // lambda broke up-to-date
        val action = object : Action<Task> {
            override fun execute(t: Task) {
                val kotlinBinary: RegularFile =
                    (kotlinBinaryField.get(packTask) as RegularFileProperty).get()
                val destinationDir: Directory =
                    (destinationDirField.get(packTask) as DirectoryProperty).get()
                val executablePath: String =
                    (executablePathField.get(packTask) as Provider<*>).get().toString()

                val outputDir: File = File(destinationDir.asFile, executablePath).parentFile

                val bundleSearchDir: File = kotlinBinary.asFile.parentFile
                bundleSearchDir
                    .listFiles { file: File -> file.extension == "bundle" }
                    ?.forEach { file ->
                        file.copyRecursively(File(outputDir, file.name), true)
                    }
            }
        }
        doLast(action)
    }
