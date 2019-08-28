/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.net.URI

pluginManagement {
    repositories {
        jcenter()
        google()

        maven { url = URI("https://dl.bintray.com/kotlin/kotlin") }
        maven { url = URI("https://kotlin.bintray.com/kotlinx") }
    }
    resolutionStrategy.eachPlugin {
        val module = Deps.plugins[requested.id.id] ?: return@eachPlugin

        useModule(module)
    }
}

enableFeaturePreview("GRADLE_METADATA")

include(":resources")
include(":sample:android-app")
include(":sample:mpp-library")
