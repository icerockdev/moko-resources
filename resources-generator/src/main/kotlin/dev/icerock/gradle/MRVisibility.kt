/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.squareup.kotlinpoet.KModifier

enum class MRVisibility {
    Public,
    Internal
}

internal fun MRVisibility.toModifier(): KModifier = when (this) {
    MRVisibility.Public -> KModifier.PUBLIC
    MRVisibility.Internal -> KModifier.INTERNAL
}
