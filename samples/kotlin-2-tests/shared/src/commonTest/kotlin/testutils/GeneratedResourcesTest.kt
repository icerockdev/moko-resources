/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package testutils

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import template.composemultiplatform.shared.SharedRes
import kotlin.test.Test

class GeneratedResourcesTest : MultiPlatformTest() {

    @Test
    fun readBuildInfoTest() = runTest {
        getTestLocalizer().localize(SharedRes.strings.appName)
            .shouldBe("ExampleApp")
    }
}