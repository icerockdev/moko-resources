/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

import dev.icerock.moko.resources.provider.RemoteJsStringLoader

interface RemoteJsStringLoaderHolder {
    val stringsLoader: RemoteJsStringLoader
}
