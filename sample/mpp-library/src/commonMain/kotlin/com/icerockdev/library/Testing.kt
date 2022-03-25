/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import com.icerockdev.library.nested.nestedFile
import com.icerockdev.library.nested.nestedTest
import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.FontResource
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
import dev.icerock.moko.resources.getAssetByFilePath
import dev.icerock.moko.resources.getImageByFileName

@Suppress("MagicNumber", "TooManyFunctions")
public object Testing {
    public fun getStrings(): List<StringDesc> {
        return listOf(
            MR.strings.test_simple.desc(),
            MR.strings.test2.desc(),
            MR.strings.test3.desc(),
            MR.strings.common_name.desc(),
            "some raw string".desc(),
            // 0 on android in english will be `other`
            // 0 on ios in english will be `zero`
            // to not break tests - i just remove this case from list
//            MR.plurals.test_plural.desc(0),
            MR.plurals.test_plural.desc(1),
            MR.plurals.test_plural.desc(2),
            MR.plurals.test_plural.desc(3),
            MR.strings.multilined.desc(),
            MR.strings.quotes.desc(),
            nestedTest(),
            MR.plurals.test_plural.desc(7),
            MR.plurals.test_plural_interop.desc(1),
            MR.plurals.test_plural_interop.desc(2),
            MR.plurals.test_plural_interop.desc(7),
        )
    }

    public fun getDrawable(): ImageResource {
        return MR.images.home_black_18
    }

    public fun getDrawableByFileName(): ImageResource? {
        return MR.images.getImageByFileName("home_black_18")
    }

    public fun getStringDesc(): StringDesc {
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

    public fun getFontTtf1(): FontResource = MR.fonts.Raleway.italic

    public fun getFontTtf2(): FontResource = MR.fonts.Raleway.bold

    public fun getFontOtf1(): FontResource = MR.fonts.cormorant.regular
    public fun getFontOtf2(): FontResource = MR.fonts.cormorant.italic
    public fun getFontOtf3(): FontResource = MR.fonts.cormorant.light

    public fun getTextsFromAssets(): List<AssetResource> {
        return listOf(
            MR.assets.test_1,
            MR.assets.getAssetByFilePath("texts/test2.txt") ?: error("Can't load asset"),
            MR.assets.texts.inner_1.test3
        )
    }

    public fun locale(lang: String?) {
        StringDesc.localeType = if (lang != null) StringDesc.LocaleType.Custom(lang)
        else StringDesc.LocaleType.System
    }

    public fun getTextFile(): FileResource {
        return MR.files.test
    }

    public fun getJsonFile(): FileResource {
        return MR.files.some
    }

    public fun getNestedJsonFile(): FileResource {
        return nestedFile()
    }

    public fun getGradientColors(): List<ColorResource> {
        return listOf(
            MR.colors.valueColor,
            MR.colors.valueColor2,
        )
    }

    public fun getTextColor(): ColorResource {
        return MR.colors.textColor
    }

    public fun getPlurals(): StringDesc {
        return List(26) { number ->
            val value = number + 1
            MR.plurals.myPlural.format(value, value)
        }.joinToStringDesc("\n")
    }
}
