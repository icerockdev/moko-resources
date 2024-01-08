///*
// * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator.jsJvmCommon
//
//import java.io.File
//
//fun generateHighestQualityImageResources(
//    resourcesGenerationDir: File,
//    keyFileMap: Map<String, List<File>>,
//    imagesDirName: String
//) {
//    val imagesDir = File(resourcesGenerationDir, imagesDirName).apply { mkdirs() }
//
//    keyFileMap.forEach { (key, files) ->
//        val hqFile = files.maxByOrNull {
//            it.nameWithoutExtension.substringAfter("@").substringBefore("x").toDouble()
//        } ?: return
//        hqFile.copyTo(File(imagesDir, "$key.${hqFile.extension}"))
//    }
//}
