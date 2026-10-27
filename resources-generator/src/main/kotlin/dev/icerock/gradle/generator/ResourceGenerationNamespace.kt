/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

/**
 * Names generated artifacts that can coexist in the same test binary.
 *
 * Main resources use [unqualified]. Test resources use [isolated] when the parallel main hierarchy
 * also contains resources. Keeping all derived names here prevents generated object names,
 * localization files, and Apple provider references from drifting apart.
 */
internal class ResourceGenerationNamespace private constructor(
    private val sourceSetSuffix: String?,
) {
    val isIsolated: Boolean = sourceSetSuffix != null

    val localizationFileSuffix: String = sourceSetSuffix?.let { "_$it" }.orEmpty()

    val applePlatformDetailsProviderName: String =
        Constants.PlatformDetails.providerObjectName + sourceSetSuffix.orEmpty()

    val applePlatformDetailsProviderReference: String =
        Constants.PlatformDetails.providerReferenceFor(applePlatformDetailsProviderName)

    val appleBundleReference: String =
        Constants.Apple.providerBundleReferenceFor(applePlatformDetailsProviderReference)

    fun qualifyResourceObjectName(baseName: String): String =
        baseName + sourceSetSuffix.orEmpty()

    companion object {
        val unqualified: ResourceGenerationNamespace = ResourceGenerationNamespace(
            sourceSetSuffix = null,
        )

        fun isolated(sourceSetName: String): ResourceGenerationNamespace {
            require(sourceSetName.isNotBlank()) { "Source-set name must not be blank" }
            return ResourceGenerationNamespace(sourceSetSuffix = sourceSetName)
        }
    }
}
