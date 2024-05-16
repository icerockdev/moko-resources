/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.string

import dev.icerock.gradle.metadata.resource.StringMetadata

internal fun List<StringMetadata>.processLanguages(): Map<String, Map<String, String>> {
    return this.flatMap { metadata ->
        metadata.values.map { localeItem ->
            localeItem.locale to (metadata.key to localeItem.value)
        }
    }.groupBy(
        keySelector = { it.first },
        valueTransform = { it.second }
    ).mapValues { it.value.toMap() }
}
