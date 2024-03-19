/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.plural

import dev.icerock.gradle.metadata.resource.PluralMetadata

internal fun List<PluralMetadata>.processLanguages(): Map<String, Map<String, Map<String, String>>> {
    return this.flatMap { metadata ->
        metadata.values.map { localeItem ->
            val pluralMap: Map<String, String> = localeItem.values
                .associate { it.quantity.name.lowercase() to it.value }
            localeItem.locale to (metadata.key to pluralMap)
        }
    }.groupBy(
        keySelector = { it.first },
        valueTransform = { it.second }
    ).mapValues { it.value.toMap() }
}
