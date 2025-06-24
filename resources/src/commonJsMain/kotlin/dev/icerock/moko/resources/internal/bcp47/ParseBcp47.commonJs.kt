/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal.bcp47

import dev.icerock.moko.resources.internal.JsonElement

/**
 * Parse with bcp47 to json map
 */
internal expect fun parseBcp47(tag: String): JsonElement.Object

