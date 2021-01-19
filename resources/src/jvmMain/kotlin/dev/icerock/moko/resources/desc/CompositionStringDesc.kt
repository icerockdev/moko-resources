package dev.icerock.moko.resources.desc

actual class CompositionStringDesc actual constructor(
    private val args: Iterable<StringDesc>,
    private val separator: String?
) : StringDesc {

    override fun localized() = args.joinToString(separator = separator ?: "") { it.localized() }
}