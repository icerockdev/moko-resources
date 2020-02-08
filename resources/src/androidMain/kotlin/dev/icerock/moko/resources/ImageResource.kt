/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes

actual class ImageResource(
    @DrawableRes val drawableResId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(drawableResId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ImageResource> {
        override fun createFromParcel(parcel: Parcel): ImageResource {
            return ImageResource(parcel)
        }

        override fun newArray(size: Int): Array<ImageResource?> {
            return arrayOfNulls(size)
        }
    }
}
