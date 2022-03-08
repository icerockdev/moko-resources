/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import dev.icerock.moko.resources.desc.StringDesc

actual suspend fun StringDesc.getString(): String = localized()
