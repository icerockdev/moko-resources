/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.localizedAsync

actual fun StringDesc.getString(): String {
    return localized()
}

actual suspend fun StringDesc.getStringAsync(): String = localizedAsync()
