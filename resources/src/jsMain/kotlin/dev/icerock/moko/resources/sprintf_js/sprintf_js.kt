/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JsModule("sprintf-js")
package dev.icerock.moko.resources.sprintf_js

external fun sprintf(format: String, vararg args: Any): String