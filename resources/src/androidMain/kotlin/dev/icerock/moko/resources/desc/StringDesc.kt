/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

actual sealed class StringDesc {
    actual data class Resource actual constructor(val stringRes: StringResource) : StringDesc() {
        override fun toString(context: Context): String {
            return context.getString(stringRes.resourceId)
        }
    }

    actual data class ResourceFormatted actual constructor(
        val stringRes: StringResource,
        val args: List<Any>
    ) : StringDesc() {
        override fun toString(context: Context): String {
            return context.getString(stringRes.resourceId, *(args.toTypedArray()))
        }

        actual constructor(stringRes: StringResource, vararg args: Any) : this(
            stringRes,
            args.toList()
        )
    }

    actual data class Plural actual constructor(val pluralsRes: PluralsResource, val number: Int) :
        StringDesc() {
        override fun toString(context: Context): String {
            return context.resources.getQuantityString(pluralsRes.resourceId, number)
        }
    }

    actual data class PluralFormatted actual constructor(
        val pluralsRes: PluralsResource,
        val number: Int,
        val args: List<Any>
    ) : StringDesc() {
        override fun toString(context: Context): String {
            return context.resources.getQuantityString(
                pluralsRes.resourceId,
                number,
                *(args.toTypedArray())
            )
        }

        actual constructor(pluralsRes: PluralsResource, number: Int, vararg args: Any) : this(
            pluralsRes,
            number,
            args.toList()
        )
    }

    actual data class Raw actual constructor(val string: String) : StringDesc() {
        override fun toString(context: Context): String {
            return string
        }
    }

    actual data class Composition actual constructor(val args: List<StringDesc>) : StringDesc() {
        override fun toString(context: Context): String {
            return StringBuilder().apply {
                args.forEach {
                    append(it.toString(context))
                }
            }.toString()
        }
    }

    abstract fun toString(context: Context): String
}
