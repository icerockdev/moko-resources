package testutils

import dev.icerock.moko.resources.StringResource

expect abstract class ResourceLocalizationTestBase() {
    suspend fun localize(resource: StringResource, vararg args: Any?): String
}
