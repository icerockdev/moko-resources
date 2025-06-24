buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()
    }
    dependencies {
        classpath(moko.resourcesGradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.5.11")
    }
}
