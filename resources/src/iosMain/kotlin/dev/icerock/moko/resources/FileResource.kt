/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

actual class FileResource(
    val fileName: String,
    val bundle: NSBundle = NSBundle.mainBundle
) {
    val path: String get() = bundle.pathForResource(name = fileName, ofType = null, inDirectory = "files")!!
    val url: NSURL get() = bundle.URLForResource(name = fileName, withExtension = null, subdirectory = "files")!!

    fun readText(): String {
        val (result: String?, error: NSError?) = memScoped {
            val p = alloc<ObjCObjectVar<NSError?>>()
            val result: String? = runCatching {
                NSString.stringWithContentsOfFile(
                    path = path,
                    encoding = NSUTF8StringEncoding,
                    error = p.ptr
                )
            }.getOrNull()
            result to p.value
        }

        if (error != null) throw ReadFileTextException(fileResource = this, error = error)
        else return result!!
    }
}
