/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.util.Locale
import java.util.ResourceBundle

fun ClassLoader.getResourceBundle(bundleName: String, locale: Locale): ResourceBundle {
    return ResourceBundle.getBundle(
        bundleName,
        locale,
        this,
        // Otherwise, the default locale will be picked from system settings
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT)
    )
}
