/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

expect sealed class StringDesc {
    class Resource(stringRes: StringResource) : StringDesc
    class ResourceFormatted(stringRes: StringResource, args: List<Any>) : StringDesc {
        constructor(stringRes: StringResource, vararg args: Any)
    }

    class Plural(pluralsRes: PluralsResource, number: Int) : StringDesc
    class PluralFormatted(pluralsRes: PluralsResource, number: Int, args: List<Any>) : StringDesc {
        constructor(pluralsRes: PluralsResource, number: Int, vararg args: Any)
    }

    class Raw(string: String) : StringDesc
    class Composition(args: List<StringDesc>, separator: String? = null) : StringDesc

    sealed class LocaleType {
        object System : LocaleType
        class Custom(locale: String) : LocaleType
    }

    companion object {
        var localeType: LocaleType
    }
}

fun String.desc() = StringDesc.Raw(this)
fun StringResource.desc() = StringDesc.Resource(this)
fun PluralsResource.desc(number: Int) = StringDesc.Plural(this, number)

operator fun StringDesc.plus(other: StringDesc): StringDesc {
    return StringDesc.Composition(listOf(this, other))
}
