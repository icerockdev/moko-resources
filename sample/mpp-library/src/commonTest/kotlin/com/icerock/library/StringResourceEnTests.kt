/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerock.library

import com.icerockdev.library.MR
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlin.test.Test

class StringResourceEnTests : BaseStringResourceTests("en") {

    @Test
    fun checkSimpleString() = stringTest(
        expected = "Test Project",
        actual = MR.strings.common_name.desc()
    )

    @Test
    fun checkFormattedString() = stringTest(
        expected = "Test data 2",
        actual = MR.strings.format.format(2)
    )
}
