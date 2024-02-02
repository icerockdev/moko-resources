package dev.icerock.gradle.generator.platform.android

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet

@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun setupAndroidTasks(
    target: KotlinTarget,
    sourceSet: KotlinSourceSet,
    genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
    compilation: KotlinCompilation<*>,
) {
    if (target !is KotlinAndroidTarget) return

    compilation as KotlinJvmAndroidCompilation

    val project: Project = target.project

    val androidSourceSet: AndroidSourceSet = project.findAndroidSourceSet(sourceSet)
        ?: throw GradleException("can't find android source set for $sourceSet")

    // save android sourceSet name to skip build type specific tasks
    @Suppress("UnstableApiUsage")
    genTaskProvider.configure { it.androidSourceSetName.set(androidSourceSet.name) }

    // connect generateMR task with android preBuild
    @Suppress("DEPRECATION")
    val androidVariant: BaseVariant = compilation.androidVariant
    androidVariant.preBuildProvider.configure { it.dependsOn(genTaskProvider) }

    // TODO this way do more than required - we trigger generate all android related resources at all
    project.tasks.withType<AndroidLintAnalysisTask>().configureEach {
        it.dependsOn(genTaskProvider)
    }

    project.tasks.withType<LintModelWriterTask>().configureEach {
        it.dependsOn(genTaskProvider)
    }
}
