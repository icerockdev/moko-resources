package dev.icerock.gradle.metadata

import dev.icerock.gradle.generator.MRGenerator.Settings
import dev.icerock.gradle.utils.capitalize

internal fun getInterfaceName(targetName: String, generatorType: GeneratorType): String {
    return targetName.capitalize() + generatorType.name.capitalize()
}

internal fun resourcesIsEmpty(
    inputMetadata: MutableList<GeneratedObject>,
    settings: Settings,
): Boolean {
    return inputMetadata.isEmptyMetadata()
            && settings.ownResourcesFileTree.files.none { it.isFile }
}