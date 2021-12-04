plugins {
    val resourcesVersion: String = libs.versions.mokoResourcesVersion.get()

    kotlin("multiplatform") version "1.5.31" apply false
    id("org.jetbrains.compose") version "1.0.0" apply false
    id("dev.icerock.mobile.multiplatform-resources") version resourcesVersion apply false
}

group = "me.amikhailov"
version = "1.0"
