package template.composemultiplatform.shared.common.resources

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.format
import dev.icerock.moko.resources.provider.JsStringProvider

actual class Localizer(private val strings: JsStringProvider) {
    actual fun localize(res: StringResource, vararg args: Any?) = res.format(args).toLocalizedString(strings)
}
