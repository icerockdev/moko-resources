/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.strings

class EqualStringKeysException(
    val keys: List<String>
) : Exception("Can't process keys which equals their value: ${keys.joinToString { "\"$it\"" }}")
