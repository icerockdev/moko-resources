/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

actual sealed class StringDesc {
    protected fun processArgs(args: List<Any>, context: Context): Array<out Any> {
        return args.toList().map { (it as? StringDesc)?.toString(context) ?: it }.toTypedArray()
    }

    actual data class Resource actual constructor(val stringRes: StringResource) : StringDesc(), Parcelable {
        override fun toString(context: Context): String {
            return context.getString(stringRes.resourceId)
        }

        // android parcelable
        constructor(parcel: Parcel) : this(
            stringRes = parcel.readParcelable<StringResource>(StringResource::class.java.classLoader)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(stringRes, flags)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Resource> {
            override fun createFromParcel(parcel: Parcel): Resource {
                return Resource(parcel)
            }

            override fun newArray(size: Int): Array<Resource?> {
                return arrayOfNulls(size)
            }
        }
    }

    actual data class ResourceFormatted actual constructor(
        val stringRes: StringResource,
        val args: List<Any>
    ) : StringDesc() {
        override fun toString(context: Context): String {
            return context.getString(
                stringRes.resourceId, *processArgs(args, context)
            )
        }

        actual constructor(stringRes: StringResource, vararg args: Any) : this(
            stringRes,
            args.toList()
        )
    }

    actual data class Plural actual constructor(
        val pluralsRes: PluralsResource,
        val number: Int
    ) : StringDesc(), Parcelable {
        override fun toString(context: Context): String {
            return context.resources.getQuantityString(pluralsRes.resourceId, number)
        }

        // android parcelable
        constructor(parcel: Parcel) : this(
            pluralsRes = parcel.readParcelable(PluralsResource::class.java.classLoader),
            number = parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(pluralsRes, flags)
            parcel.writeInt(number)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Plural> {
            override fun createFromParcel(parcel: Parcel): Plural {
                return Plural(parcel)
            }

            override fun newArray(size: Int): Array<Plural?> {
                return arrayOfNulls(size)
            }
        }
    }

    actual data class PluralFormatted actual constructor(
        val pluralsRes: PluralsResource,
        val number: Int,
        val args: List<Any>
    ) : StringDesc() {
        override fun toString(context: Context): String {
            return context.resources.getQuantityString(
                pluralsRes.resourceId,
                number,
                *processArgs(args, context)
            )
        }

        actual constructor(pluralsRes: PluralsResource, number: Int, vararg args: Any) : this(
            pluralsRes,
            number,
            args.toList()
        )
    }

    actual data class Raw actual constructor(
        val string: String
    ) : StringDesc(), Parcelable {
        override fun toString(context: Context): String {
            return string
        }

        // android parcelable
        constructor(parcel: Parcel) : this(
            string = parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(string)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Raw> {
            override fun createFromParcel(parcel: Parcel): Raw {
                return Raw(parcel)
            }

            override fun newArray(size: Int): Array<Raw?> {
                return arrayOfNulls(size)
            }
        }
    }

    actual data class Composition actual constructor(val args: List<StringDesc>, val separator: String?) :
        StringDesc() {
        override fun toString(context: Context): String {
            return StringBuilder().apply {
                args.forEachIndexed { index, stringDesc ->
                    if (index != 0 && separator != null) {
                        append(separator)
                    }
                    append(stringDesc.toString(context))
                }
            }.toString()
        }
    }

    abstract fun toString(context: Context): String
}
