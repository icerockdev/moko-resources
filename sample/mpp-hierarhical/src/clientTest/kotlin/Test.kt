/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("IllegalIdentifier")

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

    @Test
    fun `test multiline string`() {
        val stringDesc = MR.strings.multilined.desc()
        val rawString = stringDesc.getString()
        assertEquals(expected = """first line
second line
third line.""", actual = rawString)
    }

    @Test
    fun `test quotes string`() {
        val stringDesc = MR.strings.quotes.desc()
        val rawString = stringDesc.getString()
        assertEquals(expected = """Alex009 said "hello world" & "write tests".""", actual = rawString)
    }

    @Test
    fun `test single quotes string`() {
        val stringDesc = MR.strings.single_quotes.desc()
        val rawString = stringDesc.getString()
        assertEquals(expected = """Alex009 said 'hello'""", actual = rawString)
    }
}
