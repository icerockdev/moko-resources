package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.resources.PluralsResource

actual class PluralStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int,
) : StringDesc, Parcelable {

    override fun localized() = MokoBundle.getPluralString(
        pluralsRes.key,
        number,
        pluralsRes.numberFormat[pluralsRes.key] ?: throw IllegalStateException("Unknown plural description")
    )
}