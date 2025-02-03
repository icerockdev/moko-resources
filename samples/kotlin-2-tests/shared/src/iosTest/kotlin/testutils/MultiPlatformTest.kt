package testutils

import template.composemultiplatform.shared.common.resources.Localizer

actual abstract class MultiPlatformTest {
    actual suspend fun getTestLocalizer() = Localizer()
}
