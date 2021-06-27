/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.gradle.maven-publish")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing.publications.withType<MavenPublication> {
    // Stub javadoc.jar artifact
    artifact(javadocJar.get())
}
