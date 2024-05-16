/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
internal actual fun readContentOfFile(
    filePath: String,
    error: CPointer<ObjCObjectVar<NSError?>>?,
): String? {
    return NSString.stringWithContentsOfFile(
        path = filePath,
        encoding = NSUTF8StringEncoding,
        error = error
    )
}
