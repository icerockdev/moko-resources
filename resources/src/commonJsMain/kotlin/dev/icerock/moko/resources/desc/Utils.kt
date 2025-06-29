/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.provider.JsStringProvider

object Utils {
    fun processArgs(
        args: List<Any>,
        provider: JsStringProvider
    ): Array<out Any> {
        return args.map { (it as? StringDesc)?.toLocalizedString(provider) ?: it }.toTypedArray()
    }
}
