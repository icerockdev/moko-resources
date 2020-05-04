/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

enableFeaturePreview("GRADLE_METADATA")

val properties: Map<String, String> = startParameter.projectProperties
val libraryPublish: Boolean = properties.containsKey("libraryPublish")

include(":resources")
include(":gradle-plugin")
if (!libraryPublish) {
    include(":sample:android-app")
    include(":sample:mpp-library", ":sample:mpp-library:nested-module")
    include(":sample:mpp-conditional")
    include(":sample:mpp-hierarhical")
    include(":sample:mpp-mixed")
}
