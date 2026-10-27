package testutils

import dev.icerock.moko.resources.StringResource
import template.composemultiplatform.shared.common.resources.Localizer

actual abstract class ResourceLocalizationTestBase {
    actual suspend fun localize(resource: StringResource, vararg args: Any?): String {
        val localizer = Localizer(resource.loader.getOrLoad())
        return localizer.localize(resource, *args)
    }
}
