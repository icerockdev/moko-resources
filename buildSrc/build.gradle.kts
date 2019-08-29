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
    implementation("com.squareup:kotlinpoet:1.3.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
