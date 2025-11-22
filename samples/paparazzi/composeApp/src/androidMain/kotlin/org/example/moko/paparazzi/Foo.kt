/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package org.example.moko.paparazzi

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
internal fun Foo() {
    Text(
        text = stringResource(R.string.hello_key)
    )
}
