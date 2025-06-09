/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.w3c.dom.MediaQueryList
import org.w3c.dom.Window
import org.w3c.dom.events.EventListener

private const val MEDIA_DARK_SCHEME = "(prefers-color-scheme: dark)"

internal fun Window.isDarkMode(): Boolean {
    return matchMedia(MEDIA_DARK_SCHEME).matches
}

internal fun Window.getDarkModeFlow(): Flow<Boolean> {
    return channelFlow {
        val mediaList: MediaQueryList = window.matchMedia(MEDIA_DARK_SCHEME)
        val listener = EventListener {
            trySend(isDarkMode())
        }
        mediaList.addListener(listener)

        invokeOnClose {
            mediaList.removeListener(listener)
        }
    }
}
