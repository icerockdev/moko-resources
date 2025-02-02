package testutils

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import template.composemultiplatform.shared.common.resources.Localizer

@RunWith(RobolectricTestRunner::class)
actual abstract class MultiPlatformTest {
    private val context = RuntimeEnvironment.getApplication()
    actual suspend fun getTestLocalizer() = Localizer(context)
}
