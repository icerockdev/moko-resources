package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.TypeSpec

interface ObjectBodyExtendable {
    fun extendObjectBody(classBuilder: TypeSpec.Builder)
}
