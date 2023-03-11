/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import dev.icerock.moko.resources.AssetResource

@Composable
expect fun AssetResource.readTextAsState(): State<String>
