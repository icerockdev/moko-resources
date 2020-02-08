/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

actual class FontResource(
    @FontRes
    private val fontResourceId: Int
) : Parcelable {

    fun getTypeface(context: Context): Typeface? {
        return ResourcesCompat.getFont(context, fontResourceId)
    }

    // android parcelable implementation
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(fontResourceId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FontResource> {
        override fun createFromParcel(parcel: Parcel): FontResource {
            return FontResource(parcel)
        }

        override fun newArray(size: Int): Array<FontResource?> {
            return arrayOfNulls(size)
        }
    }
}
