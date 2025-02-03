/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

object Utils {
    fun processArgs(args: List<Any>): Array<out Any> {
        return args.map { (it as? StringDesc)?.localized() ?: it }.toTypedArray()
    }
}
