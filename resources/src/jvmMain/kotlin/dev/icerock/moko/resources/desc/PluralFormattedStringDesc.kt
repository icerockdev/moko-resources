package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource

actual class PluralFormattedStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int,
    val args: List<Any>
) : StringDesc {

    override fun localized() =
        MokoBundle.getPluralString(
            pluralsRes.key,
            number,
            pluralsRes.numberFormat[pluralsRes.key]
                ?: throw IllegalStateException("Unknown plural description"),
            args
        )
}