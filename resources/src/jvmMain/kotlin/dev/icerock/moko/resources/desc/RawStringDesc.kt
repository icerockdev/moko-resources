package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable

actual class RawStringDesc actual constructor(private val string: String) : StringDesc, Parcelable {
    override fun localized() = string
}