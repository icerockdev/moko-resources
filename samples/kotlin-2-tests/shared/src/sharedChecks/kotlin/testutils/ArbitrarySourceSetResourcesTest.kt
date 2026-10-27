/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package testutils

import template.composemultiplatform.shared.SharedRessharedChecks
import template.composemultiplatform.shared.shared_checks_value
import kotlin.test.Test
import kotlin.test.assertNotNull

class ArbitrarySourceSetResourcesTest {

    @Test
    fun generatesResourcesForArbitrarilyNamedTestSourceSet() {
        assertNotNull(SharedRessharedChecks.strings.shared_checks_value)
    }
}
