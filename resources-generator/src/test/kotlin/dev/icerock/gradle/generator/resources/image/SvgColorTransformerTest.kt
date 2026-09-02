/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

import org.junit.Test
import kotlin.test.assertEquals

class SvgColorTransformerTest {

    @Test
    fun `test transform fill attribute 8-digit`() {
        val input = """<path fill="#a3acb1a4"/>"""
        val expected = """<path fill="#a3acb1" fill-opacity="0.64"/>"""
        assertEquals(expected, SvgColorTransformer.transform(input))
    }

    @Test
    fun `test transform fill attribute 4-digit`() {
        val input = """<path fill="#f008"/>"""
        val expected = """<path fill="#ff0000" fill-opacity="0.53"/>"""
        assertEquals(expected, SvgColorTransformer.transform(input))
    }

    @Test
    fun `test keep color attribute 8-digit unchanged`() {
        // color attribute doesn't have a corresponding color-opacity attribute in SVG
        val input = """<path color="#a3acb1a4"/>"""
        val expected = input
        assertEquals(expected, SvgColorTransformer.transform(input))
    }

    @Test
    fun `test transform style attribute 8-digit`() {
        val input = """<path style="fill:#a3acb1a4;stroke:#ffffff12"/>"""
        val expected = """<path style="fill:#a3acb1;fill-opacity:0.64;stroke:#ffffff;stroke-opacity:0.07"/>"""
        assertEquals(expected, SvgColorTransformer.transform(input))
    }

    @Test
    fun `test transform style attribute 4-digit`() {
        val input = """<path style="fill:#f008;stroke:#fff1"/>"""
        val expected = """<path style="fill:#ff0000;fill-opacity:0.53;stroke:#ffffff;stroke-opacity:0.07"/>"""
        assertEquals(expected, SvgColorTransformer.transform(input))
    }

    @Test
    fun `test user provided sample`() {
        val input = """
            <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 54 48"
                width="54" height="48"
                id="vector">
                <path
                    id="path"
                    d="M 54 24 C 54 37.25 41.88 48 26.93 48 C -8.99 46.7 -8.98 1.3 26.93 0 C 41.88 0 54 10.75 54 24 Z"
                    fill="#a3acb1a4"/>
            </svg>
        """.trimIndent()
        val expected = """
            <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 54 48"
                width="54" height="48"
                id="vector">
                <path
                    id="path"
                    d="M 54 24 C 54 37.25 41.88 48 26.93 48 C -8.99 46.7 -8.98 1.3 26.93 0 C 41.88 0 54 10.75 54 24 Z"
                    fill="#a3acb1" fill-opacity="0.64"/>
            </svg>
        """.trimIndent()
        assertEquals(expected, SvgColorTransformer.transform(input))
    }
}
