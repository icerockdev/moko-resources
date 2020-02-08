/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.PluralsRes

actual class PluralsResource(
    @PluralsRes val resourceId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(resourceId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PluralsResource> {
        override fun createFromParcel(parcel: Parcel): PluralsResource {
            return PluralsResource(parcel)
        }

        override fun newArray(size: Int): Array<PluralsResource?> {
            return arrayOfNulls(size)
        }
    }

}
