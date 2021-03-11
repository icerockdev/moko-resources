/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.StringsGenerator
import org.gradle.api.file.FileTree

class CommonStringsGenerator(
    stringsFileTree: FileTree
) : StringsGenerator(stringsFileTree), ObjectBodyExtendable by NOPObjectBodyExtendable() {
    override fun getPropertyInitializer(
        key: String,
        baseLanguageMap: Map<KeyType, String>
    ): CodeBlock? = null
}
