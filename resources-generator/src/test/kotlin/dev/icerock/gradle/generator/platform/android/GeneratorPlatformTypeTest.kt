/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import org.junit.Test
import kotlin.test.assertEquals

class GeneratorPlatformTypeTest {

    @Test
    fun `kmp android target reported as jvm uses android generators`() {
        val actual = normalizeResourcesPlatformTypeName(
            reportedPlatformType = "jvm",
            isKmpAndroidTarget = true,
        )

        assertEquals("androidJvm", actual)
    }

    @Test
    fun `regular jvm target remains jvm`() {
        val actual = normalizeResourcesPlatformTypeName(
            reportedPlatformType = "jvm",
            isKmpAndroidTarget = false,
        )

        assertEquals("jvm", actual)
    }
}
