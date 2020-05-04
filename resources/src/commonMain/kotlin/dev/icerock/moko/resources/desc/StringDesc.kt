/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

expect interface StringDesc {

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
