/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.5.21")
    id("detekt-convention")
    id("publication-convention")
    id("com.gradle.plugin-publish") version ("0.15.0")
    id("java-gradle-plugin")
}

group = "dev.icerock.moko"
version = libs.versions.mokoResourcesVersion.get()

dependencies {
    implementation(gradleKotlinDsl())
    compileOnly(libs.kotlinGradlePlugin)
    compileOnly(libs.androidGradlePlugin)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinxSerialization)
    implementation(libs.apacheCommonsText)
    implementation(libs.kotlinCompilerEmbeddable)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

publishing.publications.register("mavenJava", MavenPublication::class) {
    from(components["java"])
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
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