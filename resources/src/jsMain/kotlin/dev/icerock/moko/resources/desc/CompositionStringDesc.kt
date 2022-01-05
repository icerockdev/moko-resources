/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

actual class CompositionStringDesc actual constructor(
    args: Iterable<StringDesc>,
    separator: String?
) : StringDesc