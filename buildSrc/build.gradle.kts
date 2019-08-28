import java.net.URI

plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()

    jcenter()
    google()

    maven { url = URI("https://dl.bintray.com/icerockdev/plugins") }
}

dependencies {
    implementation("dev.icerock:mobile-multiplatform:0.1.0")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
