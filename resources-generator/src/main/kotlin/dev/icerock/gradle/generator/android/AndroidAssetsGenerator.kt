package dev.icerock.gradle.generator.android

import dev.icerock.gradle.generator.AssetsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class AndroidAssetsGenerator(inputFile: FileTree) : AssetsGenerator(inputFile) {

    override fun generate(assetsGenerationDir: File, resourcesGenerationDir: File) {

        inputFile.files.forEach {
            it.copyTo(File(assetsGenerationDir, getBaseDir(assetsGenerationDir.name, it)))
        }
    }

    private fun getBaseDir(baseDirName: String, file: File): String {
        val relativePathToAssets = file.path.substringAfterLast(baseDirName)
        return File(relativePathToAssets).path
    }
}
