/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("Filename")
@file:JsModule("bcp-47")

package dev.icerock.moko.resources.internal.bcp47

import dev.icerock.moko.resources.internal.JsObject

external fun parse(tag: String): JsObject
