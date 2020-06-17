/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    `kotlin-dsl`
    id("org.gradle.maven-publish")
}

repositories {
    mavenLocal()

    jcenter()
    google()
}

dependencies {
    implementation(Deps.Libs.Jvm.kotlinPoet)
    implementation(Deps.Libs.Jvm.koson)
    compileOnly(Deps.Plugins.kotlinMultiplatform.module!!)
    compileOnly(Deps.Plugins.androidLibrary.module!!)
    implementation(Deps.Libs.Jvm.apacheCommonsText)
    detektPlugins(Deps.Libs.Jvm.detektFormatting)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/plugins/moko-resources-generator/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }

    publications {
        register("plugin", MavenPublication::class) {
            groupId = "dev.icerock.moko"
            artifactId = "resources-generator"
            version = Versions.Libs.MultiPlatform.mokoResources

            from(components["java"])
        }
    }
}
