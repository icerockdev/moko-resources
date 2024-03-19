/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import java.util.Enumeration
import kotlin.reflect.KClass

internal fun <T> List<T>.toEnumeration(): Enumeration<T> {
    return object : Enumeration<T> {
        var count = 0

        override fun hasMoreElements(): Boolean {
            return this.count < size
        }

        override fun nextElement(): T {
            if (this.count < size) {
                return get(this.count++)
            }
            throw NoSuchElementException("List enumeration asked for more elements than present")
        }
    }
}

internal fun <B : Any, T : B> List<B>.filterClass(typeClass: KClass<T>): List<T> {
    return this.filter { it::class == typeClass }
        .map {
            @Suppress("UNCHECKED_CAST")
            it as T
        }
}
