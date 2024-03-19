/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import com.icerockdev.library.MR.plurals
import com.icerockdev.library.MR.strings
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

@Suppress("MagicNumber", "TooManyFunctions")
public object AppleTesting {
    public fun getStrings(): List<StringDesc> {
        return listOf(
            strings.apple_target_name.desc(),
            "some raw string".desc(),
            // 0 on android in english will be `other`
            // 0 on ios in english will be `zero`
            // to not break tests - i just remove this case from list
//            MR.plurals.test_plural.desc(0),
            plurals.apple_targets_count.desc(1),
            plurals.apple_targets_count.desc(2),
            plurals.apple_targets_count.desc(3),
        )
    }
}
