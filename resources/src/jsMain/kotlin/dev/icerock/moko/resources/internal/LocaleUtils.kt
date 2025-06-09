/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

import kotlinx.browser.window

actual fun currentLocale(): String {
    return window.navigator.language
}
