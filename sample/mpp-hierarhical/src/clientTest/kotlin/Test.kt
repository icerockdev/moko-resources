/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.icerockdev.library.MR
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals

class Test : BaseUnitTest() {
    @Test
    fun `test simple resource string`() {
        val stringDesc = MR.strings.test_simple.desc()
        val rawString = stringDesc.getString()
        assertEquals(expected = "test", actual = rawString)
    }
}
