package testutils

import template.composemultiplatform.shared.common.resources.Localizer

actual abstract class MultiPlatformTest actual constructor() {
    actual suspend fun getTestLocalizer() = Localizer()
}
