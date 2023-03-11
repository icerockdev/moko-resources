/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import com.icerockdev.library.nested.MR
import dev.icerock.moko.resources.desc.desc

object Testing {
    fun test() {
        val value = MR.strings.common_name.desc()
        println(value)
    }
}
