package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.StringResource

actual class ResourceFormattedStringDesc actual constructor(
    private val stringRes: StringResource,
    private val args: List<Any>
) : StringDesc {

    override fun localized() = MokoBundle.getString(stringRes.key, args)
}