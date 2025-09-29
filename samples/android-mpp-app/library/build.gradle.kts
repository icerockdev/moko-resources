import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask


/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    androidLibrary {
        namespace = "com.icerockdev.mpplibrary"
        compileSdk = 33
        minSdk = 16
        androidResources.enable = true

        withJava() // enable java compilation support
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}

dependencies {
    commonMainImplementation(moko.resources)
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.mpplibrary")
}

//plugins.withId("com.android.kotlin.multiplatform.library") {
//    // Это есть у вас: KotlinMultiplatformAndroidComponentsExtension
//    val kmpAndroid = extensions.getByType<KotlinMultiplatformAndroidComponentsExtension>()
//
//    androidComponents.onVariants { variant ->
//        val capName = variant.name.replaceFirstChar {
//            if (it.isLowerCase()) it.titlecase() else it.toString()
//        }
//
//        val generateTask = project.tasks.named("generateMR${variant.name}")
//
//        // Provider<Directory> — не разворачиваем
//        val resDir: Provider<Directory> = generateTask.flatMap {
//            (it as dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask).outputResourcesDir
//        }
//
//        // addStaticSourceDirectory(String) — подаём Provider<String>
//        val resPath: Provider<String> = resDir.map { it.asFile.absolutePath }
//        variant.sources.res?.addStaticSourceDirectory(resPath.a)
//
//        // Связь тасок — провайдер не разворачиваем
//        project.tasks.named("merge${capName}Resources").configure {
//            dependsOn(generateTask)
//            inputs.dir(resDir) // ок, Provider<Directory>
//        }
//        project.tasks.matching { it.name == "package${capName}Resources" }.configureEach {
//            dependsOn(generateTask)
//            inputs.dir(resDir)
//        }
//    }
//}

tasks.matching { it.name == "packageAndroidMainResources" }
    .configureEach {
        dependsOn(tasks.named("generateMRandroidMain"))
    }
tasks.matching { it.name == "mergeAndroidMainAssets" }
    .configureEach {
        dependsOn(tasks.named("generateMRandroidMain"))
    }