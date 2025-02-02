package template.composemultiplatform.shared.common.resources

import android.content.Context
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.format

actual class Localizer(private val context: Context) {
    actual fun localize(res: StringResource, vararg args: Any?) = res.format(args).toString(context)
}
