/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

inline fun <reified T : Task> TaskContainer.maybeRegister(
    taskName: String,
    noinline configuration: T.() -> Unit
): TaskProvider<T> = try {
    named<T>(taskName)
} catch (@Suppress("SwallowedException") ex: UnknownTaskException) {
    register<T>(taskName, configuration)
}
