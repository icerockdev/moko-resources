buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()
    }
    dependencies {
        classpath(moko.resourcesGradlePlugin)
        classpath(libs.kotlinGradlePlugin)
        classpath(libs.composeJetBrainsPlugin)
        classpath(libs.composeCompilerPlugin)
    }
}
