/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.PluralsResource

// These simple wrappers offer parity with the Compose `pluralStringResource` that accept
// a `@PluralRes id` per https://developer.android.com/develop/ui/compose/resources#string-plurals
// in order to more closely match vanilla Compose, improving discoverability.

@Composable
fun pluralStringResource(resource: PluralsResource, quantity: Int): String {
    return stringResource(resource, quantity)
}

@Composable
fun pluralStringResource(resource: PluralsResource, quantity: Int, vararg formatArgs: Any): String {
    return stringResource(resource, quantity, *formatArgs)
}
