package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.SourceDirectorySet

class CommonAssetsGenerator(sourceDirectorySet: SourceDirectorySet) :
    AssetsGenerator(sourceDirectorySet),
    ObjectBodyExtendable by NOPObjectBodyExtendable() {
    override fun getClassModifiers(): Array<KModifier> = emptyArray()

    override fun getPropertyModifiers(): Array<KModifier> = emptyArray()

    override fun getPropertyInitializer(fileSpec: AssetSpecFile): CodeBlock? = null
}
