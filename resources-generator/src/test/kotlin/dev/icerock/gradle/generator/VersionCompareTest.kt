/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import dev.icerock.gradle.utils.hasMinimalVersion
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionCompareTest {

    @Test
    fun `current major greater than min`() {
        assertTrue(hasMinimalVersion("8.0.0", "9.0.0"))
    }

    @Test
    fun `current major less than min`() {
        assertFalse(hasMinimalVersion("9.0.0", "8.11.1"))
    }

    @Test
    fun `current minor greater than min`() {
        assertTrue(hasMinimalVersion("8.1.0", "8.11.1"))
    }

    @Test
    fun `current minor less than min`() {
        assertFalse(hasMinimalVersion("8.11.1", "8.1.0"))
    }

    @Test
    fun `current patch greater than min`() {
        assertTrue(hasMinimalVersion("8.11.0", "8.11.1"))
    }

    @Test
    fun `current patch equal to min`() {
        assertTrue(hasMinimalVersion("8.11.1", "8.11.1"))
    }

    @Test
    fun `current patch less than min`() {
        assertFalse(hasMinimalVersion("8.11.2", "8.11.1"))
    }

    @Test
    fun `current with beta greater or equal`() {
        assertTrue(hasMinimalVersion("9.0.0", "9.0.0-beta3"))
        assertTrue(hasMinimalVersion("9.0.0-beta3", "9.0.0-beta3"))
    }

    @Test
    fun `current with beta less than min`() {
        assertFalse(hasMinimalVersion("9.0.1", "9.0.0-beta3"))
    }

    @Test
    fun `minVersion without patch`() {
        assertTrue(hasMinimalVersion("9.0", "9.0.1"))
        assertTrue(hasMinimalVersion("9.0", "9.0.0-beta3"))
        assertFalse(hasMinimalVersion("9.1", "9.0.0"))
    }
}
