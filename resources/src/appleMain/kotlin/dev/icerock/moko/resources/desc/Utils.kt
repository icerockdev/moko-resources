/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.StringResource
import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

object Utils {
    fun processArgs(args: List<Any>): Array<out Any> {
        return args.map { (it as? StringDesc)?.localized() ?: it }.toTypedArray()
    }

    fun localizedString(stringRes: StringResource): String {
        val bundle = StringDesc.localeType.getLocaleBundle(stringRes.bundle)
        val string = bundle.localizedStringForKey(stringRes.resourceId, null, null)
        return if (string == stringRes.resourceId) {
            stringRes.bundle.localizedStringForKey(stringRes.resourceId, null, null)
        } else string
    }

    fun stringWithFormat(format: String, args: Array<out Any>): String {
        // NSString format works with NSObjects via %@, we should change standard format to %@
        val objcFormat = format.replace(Regex("%((?:\\.|\\d|\\$)*)[abcdefs]"), "%$1@")
        // bad but objc interop limited :(
        // When calling variadic C functions spread operator is supported only for *arrayOf(...)
        @Suppress("MagicNumber")
        return when (args.size) {
            0 -> NSString.stringWithFormat(objcFormat)
            1 -> NSString.stringWithFormat(objcFormat, args[0])
            2 -> NSString.stringWithFormat(objcFormat, args[0], args[1])
            3 -> NSString.stringWithFormat(objcFormat, args[0], args[1], args[2])
            4 -> NSString.stringWithFormat(objcFormat, args[0], args[1], args[2], args[3])
            5 -> NSString.stringWithFormat(objcFormat, args[0], args[1], args[2], args[3], args[4])
            6 -> NSString.stringWithFormat(
                objcFormat,
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5]
            )
            7 -> NSString.stringWithFormat(
                objcFormat,
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6]
            )
            8 -> NSString.stringWithFormat(
                objcFormat,
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7]
            )
            9 -> NSString.stringWithFormat(
                objcFormat,
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8]
            )
            else -> throw IllegalArgumentException("can't handle more then 9 arguments now")
        }
    }
}
