/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.android.build.gradle.BaseExtension

configure<BaseExtension> {
    compileSdkVersion(33)

    defaultConfig {
        minSdk = 16
        targetSdk = 33
    }
}
