/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.8.10")
    id("detekt-convention")
    id("publication-convention")
    id("com.gradle.plugin-publish") version ("0.21.0")
    id("java-gradle-plugin")
}

group = "dev.icerock.moko"
version = moko.versions.resourcesVersion.get()

dependencies {
    implementation(gradleKotlinDsl())
    compileOnly(libs.kotlinGradlePlugin)
    compileOnly(libs.androidGradlePlugin)
    compileOnly(libs.kotlinCompilerEmbeddable)
    compileOnly(libs.androidSdkCommon)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinxSerialization)
    implementation(libs.apacheCommonsText)
    implementation(libs.commonsCodec)
}

java {
    withJavadocJar()
    withSourcesJar()
}

afterEvaluate {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

publishing.publications.register("mavenJava", MavenPublication::class) {
    from(components["java"])
}

gradlePlugin {
    plugins {
        create("multiplatform-resources") {
            id = "dev.icerock.mobile.multiplatform-resources"
            implementationClass = "dev.icerock.gradle.MultiplatformResourcesPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/icerockdev/moko-resources"
    vcsUrl = "https://github.com/icerockdev/moko-resources"
    description = "Plugin to provide access to the resources on iOS & Android"
    tags = listOf("moko-resources", "moko", "kotlin", "kotlin-multiplatform")

    plugins {
        getByName("multiplatform-resources") {
            displayName = "MOKO resources generator plugin"
        }
    }

    mavenCoordinates {
        groupId = project.group as String
        artifactId = project.name
        version = project.version as String
    }
}
