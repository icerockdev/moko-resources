package template.composemultiplatform.shared.common.resources

import dev.icerock.moko.resources.StringResource

expect class Localizer {
    fun localize(res: StringResource, vararg args: Any?): String
}
