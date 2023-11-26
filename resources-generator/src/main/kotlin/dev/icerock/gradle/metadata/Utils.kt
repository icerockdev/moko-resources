package dev.icerock.gradle.metadata

import dev.icerock.gradle.generator.MRGenerator.Generator
import dev.icerock.gradle.utils.capitalize

internal fun getInterfaceName(targetName: String, generator: Generator): String {
    return targetName.capitalize() + generator.mrObjectName.capitalize()
}
