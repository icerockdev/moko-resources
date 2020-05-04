/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.Foundation.NSError

class ReadFileTextException(
    val fileResource: FileResource,
    val info: String
) : Exception("can't read file $fileResource text ($info)") {
    constructor(fileResource: FileResource, error: NSError) : this(fileResource, error.localizedDescription)
}
