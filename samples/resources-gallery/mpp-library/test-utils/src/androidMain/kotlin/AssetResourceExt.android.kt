/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.icerock.moko.resources.AssetResource

public actual suspend fun AssetResource.readTextContent(): String {
    val context: Context = ApplicationProvider.getApplicationContext()
    return this.readText(context)
}
