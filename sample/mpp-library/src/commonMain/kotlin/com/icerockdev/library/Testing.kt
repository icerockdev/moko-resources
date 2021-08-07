/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import com.icerockdev.library.nested.nestedFile
import com.icerockdev.library.nested.nestedTest
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.joinToStringDesc
import dev.icerock.moko.resources.desc.plus
import dev.icerock.moko.resources.format
import dev.icerock.moko.resources.getImageByFileName

@Suppress("MagicNumber", "TooManyFunctions")
object Testing {
    fun getStrings(): List<StringDesc> {
        return listOf(
            MR.strings.test_simple.desc(),
            MR.strings.test2.desc(),
            MR.strings.test3.desc(),
            MR.strings.common_name.desc(),
            "some raw string".desc(),
            MR.plurals.test_plural.desc(0),
            MR.plurals.test_plural.desc(1),
            MR.plurals.test_plural.desc(2),
            MR.plurals.test_plural.desc(3),
            MR.strings.multilined.desc(),
            MR.strings.quotes.desc(),
            nestedTest()
        )
    }

    fun getDrawable(): ImageResource {
        return MR.images.home_black_18
    }

    fun getDrawableByFileName(): ImageResource? {
        return MR.images.getImageByFileName("home_black_18")
    }

    fun getStringDesc(): StringDesc {
        // create simple string
        val simpleString = StringDesc.Resource(MR.strings.test_simple)
        val simpleStringExt = MR.strings.test_simple.desc()

        // create formatted string
        val formattedString = StringDesc.ResourceFormatted(MR.strings.format, 9)
        val formattedStringExt = MR.strings.format.format(9)

        // create plural
        val simplePlural = StringDesc.Plural(MR.plurals.test_plural, 10)
        val simplePluralExt = MR.plurals.test_plural.desc(10)

        // create formatted plural
        val formattedPlural = StringDesc.PluralFormatted(MR.plurals.my_plural, 10, 10)
        val formattedPluralExt = MR.plurals.my_plural.format(10, 10)

        // raw string
        val simpleRaw = StringDesc.Raw("raw string")
        val simpleRawExt = "raw string".desc()

        // composition
        val composition = simpleString + simpleRaw

        // create encoding
        val encoding = StringDesc.Resource(MR.strings.encoding)

        // create
        val positional = StringDesc.ResourceFormatted(MR.strings.positional, 9, "str")
        val positionalExt = MR.strings.positional.format(9, "str")

        // result as list composition
        val list = listOf(
            simpleString,
            simpleStringExt,
            formattedString,
            simplePlural,
            simplePluralExt,
            formattedPlural,
            formattedPluralExt,
            simpleRaw,
            simpleRawExt,
            composition,
            encoding,
            positional
        )

        return list.joinToStringDesc("\n")
    }

    fun getFontTtf1() = MR.fonts.Raleway.italic

    fun getFontTtf2() = MR.fonts.Raleway.bold

    fun getFontOtf1() = MR.fonts.cormorant.regular
    fun getFontOtf2() = MR.fonts.cormorant.italic
    fun getFontOtf3() = MR.fonts.cormorant.light

    fun locale(lang: String?) {
        StringDesc.localeType = if (lang != null) StringDesc.LocaleType.Custom(lang)
        else StringDesc.LocaleType.System
    }

    fun getTextFile(): FileResource {
        return MR.files.test
    }

    fun getJsonFile(): FileResource {
        return MR.files.some
    }

    fun getNestedJsonFile(): FileResource {
        return nestedFile()
    }

    fun getGradientColors(): List<ColorResource> {
        return listOf(
            MR.colors.valueColor,
            MR.colors.valueColor2,
        )
    }

    fun getTextColor(): ColorResource {
        return MR.colors.textColor
    }
}
