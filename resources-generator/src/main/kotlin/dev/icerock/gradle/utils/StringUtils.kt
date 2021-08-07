package dev.icerock.gradle.utils

val String.withoutExtension: String
    get() = substringBeforeLast(".")
