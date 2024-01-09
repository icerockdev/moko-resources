///*
// * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator.js
//
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.KModifier
//import com.squareup.kotlinpoet.PropertySpec
//import com.squareup.kotlinpoet.STRING
//import com.squareup.kotlinpoet.TypeSpec
//import dev.icerock.gradle.generator.TargetMRGenerator
//import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
//import dev.icerock.gradle.utils.calculateResourcesHash
//import dev.icerock.gradle.utils.flatName
//import org.gradle.api.Project
//import org.gradle.jvm.tasks.Jar
//import org.gradle.kotlin.dsl.withType
//import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
//import java.io.File
//
//class JsMRGenerator(
//    project: Project,
//    settings: Settings,
//    generators: List<Generator>,
//) : TargetMRGenerator(
//    project = project,
//    settings = settings,
//    generators = generators
//) {
//    private val flattenClassName: String = settings.packageName.flatName
//    override val resourcesGenerationDir: File = settings.resourcesDir.asFile
//
//    override fun processMRClass(mrClass: TypeSpec.Builder) {
//        val stringsLoaderInitializer = buildList {
//            val stringsObjectLoader = mrClass
//                .typeSpecs
//                .find { it.name == "strings" }
//                ?.propertySpecs
//                ?.find { it.name == "stringsLoader" }
//
//            val pluralsObjectLoader = mrClass
//                .typeSpecs
//                .find { it.name == "plurals" }
//                ?.propertySpecs
//                ?.find { it.name == "stringsLoader" }
//
//            if (stringsObjectLoader != null) {
//                add("strings.stringsLoader")
//            }
//            if (pluralsObjectLoader != null) {
//                add("plurals.stringsLoader")
//            }
//        }.takeIf(List<*>::isNotEmpty)
//            ?.joinToString(separator = " + ")
//
//        if (stringsLoaderInitializer != null) {
//            mrClass.addProperty(
//                PropertySpec.builder(
//                    "stringsLoader",
//                    ClassName("dev.icerock.moko.resources.provider", "RemoteJsStringLoader"),
//                ).initializer(stringsLoaderInitializer)
//                    .build()
//            )
//        }
//    }
//
//    // TODO not used. remove after complete migration of task configuration to Plugin configuration time
////    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {
////        project.tasks.withType<Kotlin2JsCompile>().configureEach {
////            it.dependsOn(generationTask)
////        }
//////        setupKLibResources(generationTask)
//////        setupResources()
////
////        // Declare task ':web-app:generateMRcommonMain' as an input of ':web-app:jsSourcesJar'.
////        project.tasks.withType<Jar>().configureEach {
////            it.dependsOn(generationTask)
////        }
////
//////        dependsOnProcessResources(
//////            project = project,
//////            sourceSet = sourceSet,
//////            task = generationTask,
//////        )
////    }
//}
