
//    // TODO not used. remove after complete migration of task configuration to Plugin configuration time
////    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {
////        project.tasks
////            .withType<KotlinCompile<*>>()
//////            .matching { it.name.contains(sourceSet.name, ignoreCase = true) }
////            .configureEach { it.dependsOn(generationTask) }
////
////        project.rootProject.tasks.matching {
////            it.name.contains("prepareKotlinBuildScriptModel")
////        }.configureEach {
////            it.dependsOn(generationTask)
////        }
////
////        project.tasks
////            .matching { it.name.startsWith("metadata") && it.name.endsWith("ProcessResources") }
////            .configureEach {
////                it.dependsOn(generationTask)
////            }
////    }

//            val generatorType: GeneratorType =
//                if (file.path.matches(StringsGenerator.STRINGS_REGEX)) {
//                    GeneratorType.Strings
//                } else if (file.path.matches(PluralsGenerator.PLURALS_REGEX)) {
//                    GeneratorType.Plurals
//                } else if (file.path.matches(ColorsGenerator.COLORS_REGEX)) {
//                    GeneratorType.Colors
//                } else if (file.parentFile.name == "images") {
//                    GeneratorType.Images
//                } else if (file.parentFile.name == "files") {
//                    GeneratorType.Files
//                } else if (file.parentFile.name == "fonts") {
//                    GeneratorType.Fonts
//                } else if (file.path.matches(AssetsGenerator.ASSETS_REGEX)) {
//                    GeneratorType.Assets
//                } else return@mapNotNull null
