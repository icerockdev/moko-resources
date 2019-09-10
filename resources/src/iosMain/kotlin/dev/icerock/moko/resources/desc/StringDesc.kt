/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

actual sealed class StringDesc {
    actual data class Resource actual constructor(val stringRes: StringResource) : StringDesc() {
        override fun toLocalizedString(formatter: Formatter): String {
            return stringRes.bundle.localizedStringForKey(stringRes.resourceId, null, null)
        }
    }

    actual data class ResourceFormatted actual constructor(
        val stringRes: StringResource,
        val args: List<Any>
    ) : StringDesc() {
        override fun toLocalizedString(formatter: Formatter): String {
            val string = stringRes.bundle.localizedStringForKey(stringRes.resourceId, null, null)
            return formatter.formatString(string, args.toTypedArray())
        }

        actual constructor(stringRes: StringResource, vararg args: Any) : this(
            stringRes,
            args.toList()
        )
    }

    actual data class Plural actual constructor(val pluralsRes: PluralsResource, val number: Int) :
        StringDesc() {
        override fun toLocalizedString(formatter: Formatter): String {
            return formatter.plural(pluralsRes, number)
        }
    }

    actual data class PluralFormatted actual constructor(
        val pluralsRes: PluralsResource,
        val number: Int,
        val args: List<Any>
    ) : StringDesc() {
        override fun toLocalizedString(formatter: Formatter): String {
            return formatter.formatPlural(pluralsRes, number, args.toTypedArray())
        }

        actual constructor(pluralsRes: PluralsResource, number: Int, vararg args: Any) : this(
            pluralsRes,
            number,
            args.toList()
        )
    }

    actual data class Raw actual constructor(val string: String) : StringDesc() {
        override fun toLocalizedString(formatter: Formatter): String {
            return string
        }
    }

    actual data class Composition actual constructor(val args: List<StringDesc>, val separator: String?) : StringDesc() {
        override fun toLocalizedString(formatter: Formatter): String {
            return StringBuilder().apply {
                args.forEachIndexed { index, stringDesc ->
                    if(index != 0 && separator != null) {
                        append(separator)
                    }
                    append(stringDesc.toLocalizedString(formatter))
                }
            }.toString()
        }
    }

    abstract fun toLocalizedString(formatter: Formatter): String

    interface Formatter {
        fun formatString(string: String, args: Array<out Any>): String
        fun plural(resource: PluralsResource, number: Int): String
        fun formatPlural(resource: PluralsResource, number: Int, args: Array<out Any>): String
    }
}
