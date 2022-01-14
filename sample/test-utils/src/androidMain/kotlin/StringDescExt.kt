/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.icerock.moko.resources.desc.StringDesc

actual fun StringDesc.getString(): String {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return toString(context)
}
