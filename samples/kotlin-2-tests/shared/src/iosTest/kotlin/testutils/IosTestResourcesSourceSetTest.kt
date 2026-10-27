/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package testutils

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import template.composemultiplatform.shared.SharedResiosTest
import template.composemultiplatform.shared.ios_test_value
import kotlin.test.Test

class IosTestResourcesSourceSetTest : ResourceLocalizationTestBase() {

    @Test
    fun iosTestResourcesAreAvailable() = runTest {
        localize(SharedResiosTest.strings.ios_test_value)
            .shouldBe("iOS test value")
    }
}
