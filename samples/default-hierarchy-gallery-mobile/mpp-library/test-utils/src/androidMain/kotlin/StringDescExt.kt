/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.icerock.moko.resources.desc.StringDesc

public actual suspend fun StringDesc.getString(): String {
    val context: Context = ApplicationProvider.getApplicationContext()
    return toString(context)
}
