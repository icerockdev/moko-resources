package dev.icerock.gradle.generator.apple

import dev.icerock.gradle.generator.AssetsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class AppleAssetsGenerator(inputFile: FileTree) : AssetsGenerator(inputFile) {

    override fun generate(assetsGenerationDir: File, resourcesGenerationDir: File) {

        inputFile.files.forEach {
            if (it.name.contains('_')) throw IllegalStateException("filename can't have underscore. We use them as separators.")
            it.copyTo(File(resourcesGenerationDir, getBaseDir(assetsGenerationDir.name, it)))
        }
    }

    private fun getBaseDir(baseDirName: String, file: File): String {
        val relativePathToAssets = file.path.substringAfterLast(baseDirName)
        var relativePathFixed = File(relativePathToAssets).path
        if (relativePathFixed.startsWith(File.separatorChar))
            relativePathFixed = relativePathFixed.substring(1)

        return relativePathFixed.replace(File.separatorChar, '_')
    }
}