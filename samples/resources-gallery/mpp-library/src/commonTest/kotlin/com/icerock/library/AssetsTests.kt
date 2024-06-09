/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerock.library

import BaseUnitTest
import com.icerockdev.library.MR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import readTextContent
import kotlin.test.Test
import kotlin.test.assertEquals

class AssetsTests : BaseUnitTest() {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun checkAssetRead() = runTest {
        assertEquals(
            expected = "Text file2 from assets.",
            actual = MR.assets.texts.test2_txt.readTextContent()
        )
    }
}
