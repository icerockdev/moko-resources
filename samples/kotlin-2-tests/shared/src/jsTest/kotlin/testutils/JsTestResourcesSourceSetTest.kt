/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package testutils

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import template.composemultiplatform.shared.SharedResjsTest
import template.composemultiplatform.shared.js_test_value
import kotlin.test.Test

class JsTestResourcesSourceSetTest : ResourceLocalizationTestBase() {

    @Test
    fun jsTestResourcesAreAvailable() = runTest {
        localize(SharedResjsTest.strings.js_test_value)
            .shouldBe("JS test value")
    }
}
