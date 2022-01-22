/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library.nested

import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

fun nestedTest(): StringDesc {
    return NestedMR.strings.nested_test.desc()
}

fun nestedFile(): FileResource {
    return NestedMR.files.nested_test
}
