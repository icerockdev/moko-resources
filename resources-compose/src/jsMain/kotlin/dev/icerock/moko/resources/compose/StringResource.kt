/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

@Composable
actual fun stringResource(resource: StringResource): String {
    val stringDesc: StringDesc = remember(resource) {
        StringDesc.Resource(resource)
    }
    return localized(stringDesc)
}

@Composable
actual fun stringResource(resource: StringResource, vararg args: Any): String {
    val stringDesc: StringDesc = remember(resource, args) {
        StringDesc.ResourceFormatted(resource, *args)
    }
    return localized(stringDesc)
}

@Composable
actual fun stringResource(resource: PluralsResource, quantity: Int): String {
    val stringDesc: StringDesc = remember(resource, quantity) {
        StringDesc.Plural(resource, quantity)
    }
    return localized(stringDesc)
}

@Composable
actual fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String {
    val stringDesc: StringDesc = remember(resource, quantity, args) {
        StringDesc.PluralFormatted(resource, quantity, *args)
    }
    return localized(stringDesc)
}

@Composable
private fun localized(stringDesc: StringDesc): String {
    return produceState(initialValue = "", stringDesc) {
        value = stringDesc.toLocalizedString()
    }.value
}
