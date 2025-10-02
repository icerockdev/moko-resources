/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.util.Base64

plugins {
    id("org.gradle.maven-publish")
}

publishing {
    publications.withType<MavenPublication> {
        // Provide artifacts information requited by Maven Central
        pom {
            name.set("MOKO resources")
            description.set("Resources access for Kotlin Multiplatform development (mobile first)")
            url.set("https://github.com/icerockdev/moko-resources")
            licenses {
                license {
                    name.set("Apache-2.0")
                    distribution.set("repo")
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
                developer {
                    id.set("warnyul")
                    name.set("Balázs Varga")
                    email.set("balazs.varga@apter.tech")
                }
            }

            scm {
                connection.set("scm:git:ssh://github.com/icerockdev/moko-resources.git")
                developerConnection.set("scm:git:ssh://github.com/icerockdev/moko-resources.git")
                url.set("https://github.com/icerockdev/moko-resources")
            }
        }
    }
}

val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
if (signingKeyId != null) {
    apply(plugin = "signing")

    configure<SigningExtension> {
        val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
        val signingKey: String? = System.getenv("SIGNING_KEY")?.let { base64Key ->
            String(Base64.getDecoder().decode(base64Key))
        }

        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications)
    }

    val signingTasks = tasks.withType<Sign>()
    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(signingTasks)
    }
}
