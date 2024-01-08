
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
