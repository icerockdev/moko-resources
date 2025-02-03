package testutils

import template.composemultiplatform.shared.common.resources.Localizer

expect abstract class MultiPlatformTest() {
    suspend fun getTestLocalizer(): Localizer
}
