/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.util.Base64
import kotlin.text.String

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.5.10")
    id("org.gradle.maven-publish")
    id("io.gitlab.arturbosch.detekt") version ("1.15.0")
    id("signing")
}

buildscript {
    repositories {
        mavenCentral()
        google()

    }
}

repositories {
    mavenCentral()
    google()
}

group = "dev.icerock.moko"
version = libs.versions.mokoResourcesVersion.get()

dependencies {
    implementation(gradleKotlinDsl())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
    compileOnly("com.android.tools.build:gradle:4.1.2")
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinxSerialization)
    implementation(libs.apacheCommonsText)
    implementation(libs.kotlinCompilerEmbeddable)
    "detektPlugins"(rootProject.libs.detektFormatting)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories.maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
        name = "OSSRH"

        credentials {
            username = System.getenv("OSSRH_USER")
            password = System.getenv("OSSRH_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            pom {
                name.set("MOKO resources gradle plugin")
                description.set("Gradle plugin for generation resources for android and iOS from common resources")
                url.set("https://github.com/icerockdev/moko-resources")
                licenses {
                    license {
                        url.set("https://github.com/icerockdev/moko-resources/blob/master/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("Alex009")
                        name.set("Aleksey Mikhailov")
                        email.set("aleksey.mikhailov@icerockdev.com")
                    }
                    developer {
                        id.set("Tetraquark")
                        name.set("Vladislav Areshkin")
                        email.set("vareshkin@icerockdev.com")
                    }
                    developer {
                        id.set("ATchernov")
                        name.set("Andrey Tchernov")
                        email.set("tchernov@icerockdev.com")
                    }
                    developer {
                        id.set("nrobi144")
                        name.set("Nagy Robert")
                        email.set("nagyrobi144@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/icerockdev/moko-resources.git")
                    developerConnection.set("scm:git:ssh://github.com/icerockdev/moko-resources.git")
                    url.set("https://github.com/icerockdev/moko-resources")
                }
            }
        }

        signing {
            val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
            val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
            val signingKey: String? = System.getenv("SIGNING_KEY")?.let { base64Key ->
                String(Base64.getDecoder().decode(base64Key))
            }
            if (signingKeyId != null) {
                useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
                sign(publishing.publications)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
