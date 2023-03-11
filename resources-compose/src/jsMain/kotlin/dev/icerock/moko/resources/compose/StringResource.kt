/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

@Composable
actual fun stringResource(resource: StringResource): String =
    produceState("") { value = StringDesc.Resource(resource).localized() }.value

@Composable
actual fun stringResource(resource: StringResource, vararg args: Any): String =
    produceState("") { value = StringDesc.ResourceFormatted(resource, *args).localized() }.value

@Composable
actual fun stringResource(resource: PluralsResource, quantity: Int): String =
    produceState("") { value = StringDesc.Plural(resource, quantity).localized() }.value

@Composable
actual fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String =
    produceState("") {
        value = StringDesc.PluralFormatted(resource, quantity, *args).localized()
    }.value
