/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.test

import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.internal.LocalizedStringLoader
import dev.icerock.moko.resources.internal.SupportedLocales

actual fun createImageResourceMock(): ImageResource {
    return ImageResource(fileUrl = "")
}

actual fun createStringResourceMock(): StringResource {
    return StringResource(
        key = "",
        loader = LocalizedStringLoader(
            supportedLocales = SupportedLocales(locales = emptyList()),
            fallbackFileUri = ""
        )
    )
}

actual fun createFileResourceMock(): FileResource {
    return FileResource(fileUrl = "")
}

actual fun createFontResourceMock(): FontResource {
    return FontResource(fileUrl = "")
}
