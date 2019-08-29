/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.resources.DrawableResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

object Testing {
    fun getStrings(): List<StringDesc> {
        return listOf(
            MR.strings.test.desc(),
            "some raw string".desc()//,
//            MR.plurals.testPlural.desc(0),
//            MR.plurals.testPlural.desc(1),
//            MR.plurals.testPlural.desc(2),
//            MR.plurals.testPlural.desc(3)
        )
    }

    fun getDrawable(): DrawableResource {
        TODO()
//        return MR.drawables.testDrawable
    }
}
