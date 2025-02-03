package template.composemultiplatform.shared.common.resources

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.format

actual class Localizer {
    actual fun localize(res: StringResource, vararg args: Any?): String {
        return res.format(args).localized()
    }
}
