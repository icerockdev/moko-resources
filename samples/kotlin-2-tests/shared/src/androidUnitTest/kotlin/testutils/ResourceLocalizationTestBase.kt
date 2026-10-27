package testutils

import dev.icerock.moko.resources.StringResource
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import template.composemultiplatform.shared.common.resources.Localizer

@RunWith(RobolectricTestRunner::class)
actual abstract class ResourceLocalizationTestBase {
    private val context = RuntimeEnvironment.getApplication()
    private val localizer = Localizer(context)

    actual suspend fun localize(resource: StringResource, vararg args: Any?): String =
        localizer.localize(resource, *args)
}
