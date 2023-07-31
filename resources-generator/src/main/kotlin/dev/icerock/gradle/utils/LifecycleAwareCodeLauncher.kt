/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * Launches [block] when populating of all [KotlinSourceSet.dependsOn] SourceSets is complete.
 */
internal fun Project.launchWhenEdgeRefinementFinalized(block: () -> Unit) {
    val version = project.plugins.getPlugin(KotlinMultiplatformPluginWrapper::class).pluginVersion
        .split(".")
        .take(2)
        .map(String::toInt)

    @Suppress("MagicNumber")
    when {
        version[0] > 1 || version[0] == 1 && version[1] >= 9 -> {
            LifecycleAwareLauncher19(this, block).launchWhenEdgeRefinementFinalized()
        }

        else -> block()
    }
}

private const val MAX_AFTER_EVALUATE_INVOCATIONS = 7

private class LifecycleAwareLauncher19(
    private val project: Project,
    private val block: () -> Unit,
) {
    private val logger = LoggerFactory.getLogger(LifecycleAwareLauncher19::class.java)
    private val stageFetcher: () -> Result<KotlinLifecycleStage> = try {
        KotlinLifecycleStageFetcher(project)
    } catch (@Suppress("TooGenericExceptionCaught") error: Throwable) {
        logger.info("Could not load required classes via reflection", error)
        val dummyFetcher: () -> Result<KotlinLifecycleStage> = { Result.failure(error) }
        dummyFetcher
    }

    fun launchWhenEdgeRefinementFinalized() = tryLaunch(0)

    private fun tryLaunch(afterEvaluateInvocations: Int) {
        val kotlinMultiplatformStageResult = stageFetcher.invoke()
        logger.debug(
            "LifecycleAwareLauncher19.tryLaunch({}). Kotlin multiplatform stage: {}",
            afterEvaluateInvocations,
            kotlinMultiplatformStageResult
        )

        if (kotlinMultiplatformStageResult.getOrNull()?.isEdgeRefinementFinalized() == true ||
            afterEvaluateInvocations >= MAX_AFTER_EVALUATE_INVOCATIONS
        ) {
            block()
            return
        }

        project.afterEvaluate {
            tryLaunch(afterEvaluateInvocations + 1)
        }
    }

    private class KotlinLifecycleStageFetcher(
        private val kotlinPluginLifecycle: Any,
        private val kotlinPluginLifecycleGetStageMethod: Method,
        private val afterFinaliseRefinesEdgesEnumOrdinal: Int,
    ) : () -> Result<KotlinLifecycleStage> {

        override fun invoke(): Result<KotlinLifecycleStage> {
            return runCatching {
                kotlinPluginLifecycleGetStageMethod.invoke(kotlinPluginLifecycle) as Enum<*>
            }.map { stage ->
                object : KotlinLifecycleStage {
                    override fun isEdgeRefinementFinalized(): Boolean {
                        return stage.ordinal >= afterFinaliseRefinesEdgesEnumOrdinal
                    }

                    override fun toString(): String =
                        "KotlinLifecycleStage(stage=$stage, edgeRefinementFinalized: ${isEdgeRefinementFinalized()})"
                }
            }
        }

        companion object {
            operator fun invoke(project: Project): KotlinLifecycleStageFetcher {
                val getLifecycleStageMethod = Class.forName(
                    "org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle"
                ).getMethod("getStage")

                val afterFinaliseRefinesEdgesOrdinal = Class.forName(
                    "org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle\$Stage"
                ).enumConstants
                    .filterIsInstance(Enum::class.java)
                    .first { it.name == "AfterFinaliseRefinesEdges" }
                    .ordinal

                val kotlinPluginLifecycle = Class.forName(
                    "org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycleKt"
                ).getMethod("getKotlinPluginLifecycle", Project::class.java)
                    .invoke(null, project)

                return KotlinLifecycleStageFetcher(
                    kotlinPluginLifecycle = kotlinPluginLifecycle,
                    kotlinPluginLifecycleGetStageMethod = getLifecycleStageMethod,
                    afterFinaliseRefinesEdgesEnumOrdinal = afterFinaliseRefinesEdgesOrdinal,
                )
            }
        }
    }

    private interface KotlinLifecycleStage {
        fun isEdgeRefinementFinalized(): Boolean
    }
}
