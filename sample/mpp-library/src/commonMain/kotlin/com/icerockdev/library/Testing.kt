/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.resources.DrawableResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.plus

object Testing {
    fun getStrings(): List<StringDesc> {
        return listOf(
            MR.strings.test.desc(),
            MR.strings.test2.desc(),
            MR.strings.test3.desc(),
            MR.strings.common_name.desc(),
            "some raw string".desc(),
            MR.plurals.test_plural.desc(0),
            MR.plurals.test_plural.desc(1),
            MR.plurals.test_plural.desc(2),
            MR.plurals.test_plural.desc(3)
        )
    }

    fun getDrawable(): DrawableResource {
        return MR.drawables.test_drawable
    }

    fun getStringDesc(): StringDesc {
        // create simple string
        val simpleString = StringDesc.Resource(MR.strings.test)
        val simpleStringExt = MR.strings.test.desc()

        // create formatted string
        val formattedString = StringDesc.ResourceFormatted(MR.strings.format, 9)

        // create plural
        val simplePlural = StringDesc.Plural(MR.plurals.test_plural, 10)
        val simplePluralExt = MR.plurals.test_plural.desc(10)

        // create formatted plural
        val formattedPlural = StringDesc.PluralFormatted(MR.plurals.my_plural, 10, 10)

        // raw string
        val simpleRaw = StringDesc.Raw("raw string")
        val simpleRawExt = "raw string".desc()

        // composition
        val composition = simpleString + simpleRaw

        // result as list composition
        val list = listOf(
            simpleString,
            simpleStringExt,
            formattedString,
            simplePlural,
            simplePluralExt,
            formattedPlural,
            simpleRaw,
            simpleRawExt,
            composition
        )

        return StringDesc.Composition(list, separator = "\n")
    }
}
