plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.20" apply false
    id("com.android.kotlin.multiplatform.library") version "8.13.0" apply false
}

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(moko.resourcesGradlePlugin)
    }
}
