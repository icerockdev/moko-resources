/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

includeBuild("plugins")
include(":resources")

include(":sample:android-app")
include(":sample:mpp-library")
// disabled while not fixed https://youtrack.jetbrains.com/issue/KT-41384
//include(":sample:mpp-library:nested-module")
include(":sample:mpp-conditional")
include(":sample:mpp-hierarhical")
include(":sample:mpp-mixed")
