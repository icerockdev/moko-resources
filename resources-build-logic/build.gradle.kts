plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()

    gradlePluginPortal()
}

dependencies {
    api(libs.mokoMultiplatformPlugin)
    api(libs.kotlinGradlePlugin)
    api(libs.androidGradlePlugin)
    api(libs.detektGradlePlugin)
    api(libs.nexusPublishing)
}
