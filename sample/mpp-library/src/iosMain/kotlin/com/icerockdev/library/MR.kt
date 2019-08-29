/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.resources.DrawableResource
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

actual object MR {
    actual object strings {
        actual val testString = StringResource("test_string")
    }

    actual object plurals {
        actual val testPlural = PluralsResource("test_plural")
    }

    actual object drawables {
        actual val testDrawable = DrawableResource("test_image")
    }
}
