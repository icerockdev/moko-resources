/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package org.example.moko.paparazzi

import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class FooTest {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testFoo() {
        paparazzi.snapshot {
            Foo()
        }
    }
}