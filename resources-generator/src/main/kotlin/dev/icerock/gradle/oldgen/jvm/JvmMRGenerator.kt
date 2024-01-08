//    // TODO not used. remove after complete migration of task configuration to Plugin configuration time
////    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {
////        project.tasks.withType<KotlinCompile>().configureEach {
////            it.dependsOn(generationTask)
////        }
////        project.tasks.withType<Jar>().configureEach {
////            it.dependsOn(generationTask)
////        }
//////        dependsOnProcessResources(
//////            project = project,
//////            sourceSet = sourceSet,
//////            task = generationTask,
//////        )
////    }
//
//}
