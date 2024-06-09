/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.StringResource
import platform.Foundation.NSBundle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleLocalizationBundleTests {

    @Test
    fun localizedStringWithLocalizationCaseTest() {
        val resource = StringResource(
            resourceId = "noResultsFound",
            bundle = NSBundle.bundleWithPath(NSBundle.mainBundle.bundlePath + "/tests.bundle")!!
        )
        StringDesc.localeType = StringDesc.LocaleType.Custom("es-US")
        val stringDesc = ResourceStringDesc(
            resource
        )
        assertEquals(
            expected = "No se han encontrado resultados",
            actual = stringDesc.localized()
        )
        StringDesc.localeType = StringDesc.LocaleType.System
    }

    @Test
    fun localizedStringMissingLocalizationCaseTest() {
        val resource = StringResource(
            resourceId = "noInternetConnection",
            bundle = NSBundle.bundleWithPath(NSBundle.mainBundle.bundlePath + "/tests.bundle")!!
        )
        StringDesc.localeType = StringDesc.LocaleType.Custom("es-US")
        val stringDesc = ResourceStringDesc(
            resource
        )
        assertEquals(
            expected = "No internet connection",
            actual = stringDesc.localized()
        )
        StringDesc.localeType = StringDesc.LocaleType.System
    }
}
