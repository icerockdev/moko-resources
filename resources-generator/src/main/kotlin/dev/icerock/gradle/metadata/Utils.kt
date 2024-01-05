//package dev.icerock.gradle.metadata
//
//import dev.icerock.gradle.generator.MRGenerator.Settings
//import dev.icerock.gradle.metadata.model.GeneratedObject
//import dev.icerock.gradle.metadata.model.GeneratorType
//import dev.icerock.gradle.utils.capitalize
//
//internal fun getInterfaceName(sourceSetName: String, generatorType: GeneratorType): String {
//    return sourceSetName.capitalize() + generatorType.name.capitalize()
//}
//
//internal fun resourcesIsEmpty(
//    inputMetadata: List<GeneratedObject>,
//    settings: Settings,
//): Boolean {
//    return inputMetadata.isEmptyMetadata()
//            && settings.ownResourcesFileTree.files.none { it.isFile }
//}