/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

@Composable
expect fun stringResource(resource: StringResource): String

@Composable
expect fun stringResource(resource: StringResource, vararg args: Any): String

@Composable
expect fun stringResource(resource: PluralsResource, quantity: Int): String

@Composable
expect fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String
