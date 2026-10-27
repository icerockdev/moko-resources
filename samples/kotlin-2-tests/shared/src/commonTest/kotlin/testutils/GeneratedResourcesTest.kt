/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package testutils

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import template.composemultiplatform.shared.SharedRes
import template.composemultiplatform.shared.SharedRescommonTest
import template.composemultiplatform.shared.appName
import template.composemultiplatform.shared.common_test_asset_txt
import template.composemultiplatform.shared.common_test_color
import template.composemultiplatform.shared.common_test_file_txt
import template.composemultiplatform.shared.common_test_image
import template.composemultiplatform.shared.common_test_plural
import template.composemultiplatform.shared.common_test_value
import kotlin.test.Test
import kotlin.test.assertNotNull

class GeneratedResourcesTest : ResourceLocalizationTestBase() {

    @Test
    fun localizeMainStringResource() = runTest {
        localize(SharedRes.strings.appName)
            .shouldBe("ExampleApp")
    }

    @Test
    fun localizeCommonTestStringResource() = runTest {
        localize(SharedRescommonTest.strings.common_test_value)
            .shouldBe("Common test value")
    }

    @Test
    fun generateAccessorsForEveryCommonTestResourceType() {
        assertNotNull(SharedRescommonTest.plurals.common_test_plural)
        assertNotNull(SharedRescommonTest.colors.common_test_color)
        assertNotNull(SharedRescommonTest.images.common_test_image)
        assertNotNull(SharedRescommonTest.files.common_test_file_txt)
        assertNotNull(SharedRescommonTest.assets.common_test_asset_txt)
    }
}
