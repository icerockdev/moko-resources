/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import com.icerockdev.library.MR
import com.icerockdev.library.hello_world

object Testing {

    fun getHelloWorld(): StringDesc {
        return MR.strings.hello_world.desc()
    }
}
