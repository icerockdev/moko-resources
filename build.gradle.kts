/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.net.URI

allprojects {
    repositories {
        mavenLocal()

        google()
        jcenter()

        maven { url = URI("https://kotlin.bintray.com/kotlin") }
        maven { url = URI("https://kotlin.bintray.com/kotlinx") }
        maven { url = URI("https://dl.bintray.com/icerockdev/moko") }
    }

    // workaround for https://youtrack.jetbrains.com/issue/KT-27170
    configurations.create("compileClasspath")
}

tasks.register("clean", Delete::class).configure {
    delete(rootProject.buildDir)
}
