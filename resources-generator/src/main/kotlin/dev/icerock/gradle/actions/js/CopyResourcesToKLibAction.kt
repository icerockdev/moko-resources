/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.js

import org.gradle.api.Action
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.File

internal class CopyResourcesToKLibAction(
    private val resourcesDirProvider: Provider<File>,
) : Action<Kotlin2JsCompile> {
    override fun execute(task: Kotlin2JsCompile) {
        val unpackedKLibDir: File = task.destinationDirectory.asFile.get()
        val defaultDir = File(unpackedKLibDir, "default")
        val resRepackDir = File(defaultDir, "resources")
        if (resRepackDir.exists().not()) return

        val resDir = File(resRepackDir, "moko-resources-js")

        resourcesDirProvider.get().copyRecursively(
            target = resDir,
            overwrite = true
        )
    }
}
