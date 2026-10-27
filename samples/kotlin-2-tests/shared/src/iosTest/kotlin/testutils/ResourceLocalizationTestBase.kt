package testutils

import dev.icerock.moko.resources.StringResource
import template.composemultiplatform.shared.common.resources.Localizer

actual abstract class ResourceLocalizationTestBase {
    private val localizer = Localizer()

    actual suspend fun localize(resource: StringResource, vararg args: Any?): String =
        localizer.localize(resource, *args)
}
