package dev.icerock.gradle.generator.platform.android

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet

// TODO cleanup code here
private const val variantsExtraName = "dev.icerock.moko.resources.android-variants"

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

    // connect generateMR task with android tasks
    val androidVariants: NamedDomainObjectContainer<Variant> = project.extra
        .get(variantsExtraName) as NamedDomainObjectContainer<Variant>

    project.logger.warn("configure $compilation with $genTaskProvider")

    androidVariants
        .matching { it.name == compilation.androidVariant.name }
        .configureEach { variant ->
            @Suppress("UnstableApiUsage")
            variant.sources.kotlin?.addGeneratedSourceDirectory(
                taskProvider = genTaskProvider,
                wiredWith = GenerateMultiplatformResourcesTask::outputSourcesDir
            )

            variant.sources.res?.addGeneratedSourceDirectory(
                taskProvider = genTaskProvider,
                wiredWith = GenerateMultiplatformResourcesTask::outputResourcesDir
            )

            variant.sources.assets?.addGeneratedSourceDirectory(
                taskProvider = genTaskProvider,
                wiredWith = GenerateMultiplatformResourcesTask::outputAssetsDir
            )
        }
}

internal fun setupAndroidVariantsSync(project: Project) {
    val androidVariants: NamedDomainObjectContainer<Variant> =
        project.objects.domainObjectContainer(Variant::class.java)

    project.extra.set(variantsExtraName, androidVariants)

    listOf(
        "com.android.application",
        "com.android.library"
    ).forEach {
        project.plugins.withId(it) {
            val componentsExtension: AndroidComponentsExtension<*, *, *> = project.extensions
                .findByType<LibraryAndroidComponentsExtension>()
                ?: project.extensions.findByType<ApplicationAndroidComponentsExtension>()
                ?: error("can't find AndroidComponentsExtension")

            componentsExtension.onVariants { variant: Variant ->
                project.logger.warn("found $variant")

                androidVariants.add(variant)
            }
        }
    }
}
