package dev.icerock.moko.resources

import java.io.FileNotFoundException

actual class FileResource(private val path: String) {

    fun readText(): String = with(Thread.currentThread().contextClassLoader) {
        getResourceAsStream(path)?.readBytes()?.decodeToString()
            ?: throw FileNotFoundException("Couldn't open resource as stream at: $path")
    }
}