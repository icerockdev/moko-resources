/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerock.library

import app.gradle9sample.library.MR
import app.gradle9sample.library.test
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test

class StringResourceEnTests : BaseStringResourceTests("en") {

    @Test
    fun checkSimpleString() = stringTest(
        expected = "Second value for Test",
        actual = MR.strings.test.desc()
    )
}
