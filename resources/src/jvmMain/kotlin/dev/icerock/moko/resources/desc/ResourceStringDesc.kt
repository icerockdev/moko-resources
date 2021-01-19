package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.resources.StringResource

actual class ResourceStringDesc actual constructor(private val stringRes: StringResource) :
    StringDesc, Parcelable {

    override fun localized(): String = MokoBundle.getString(stringRes.key)
}