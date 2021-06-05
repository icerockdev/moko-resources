/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerock.library

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.test.createColorResourceMock
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("MagicNumber")
class Testing {
    @Test
    fun `test color resource mock`() {
        val colorResource = createColorResourceMock()

        assertEquals(0, (colorResource as? ColorResource.Single)?.color?.rgba)
    }
}
