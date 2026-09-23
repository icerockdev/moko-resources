/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.data

import org.gradle.api.provider.SetProperty

@Suppress("UnnecessaryAbstractClass")
internal abstract class AppleResourceBundleRegistry {
    abstract val bundleIdentifiers: SetProperty<String>
}
