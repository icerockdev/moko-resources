package dev.icerock.gradle.generator.common

import dev.icerock.gradle.generator.AssetsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class CommonAssetsGenerator(inputFile: FileTree) : AssetsGenerator(inputFile) {

    override fun generate(assetsGenerationDir: File, resourcesGenerationDir: File) {
        // nothing.
    }
}
