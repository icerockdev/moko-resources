/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.resources.DrawableResource
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

// TODO move it to autogeneration by gradle plugin
expect object MR {
    object strings {
        val testString: StringResource
    }

    object plurals {
        val testPlural: PluralsResource
    }

    object drawables {
        val testDrawable: DrawableResource
    }
}
