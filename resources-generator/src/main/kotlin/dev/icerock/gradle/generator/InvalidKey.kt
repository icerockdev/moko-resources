/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

class InvalidKey(
    val keys: Iterable<String>,
) : Exception(
    "Keys must start with an ASCII letter or underscore and contain only ASCII letters," +
            " underscores or digits: ${keys.joinToString { "\"$it\"" }}"
)
