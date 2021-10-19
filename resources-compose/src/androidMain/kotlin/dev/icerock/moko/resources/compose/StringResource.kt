package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

@Composable
actual fun stringResource(resource: StringResource): String =
    StringDesc.Resource(resource).toString(LocalContext.current)

@Composable
actual fun stringResource(resource: StringResource, vararg args: Any): String =
    StringDesc.ResourceFormatted(resource, args).toString(LocalContext.current)

@Composable
actual fun stringResource(resource: PluralsResource, quantity: Int): String =
    StringDesc.Plural(resource, quantity).toString(LocalContext.current)

@Composable
actual fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String =
    StringDesc.PluralFormatted(resource, quantity, args).toString(LocalContext.current)
