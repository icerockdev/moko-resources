/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

// TODO move to https://github.com/icerockdev/mobile-multiplatform-gradle-plugin
data class PluginDesc(
    val id: String,
    val module: String? = null,
    val version: String? = null
)

fun DependencyHandlerScope.plugin(pluginDesc: PluginDesc): Dependency? {
    return pluginDesc.module?.let { "classpath"(it) }
}

fun DependencyHandlerScope.plugins(pluginDescList: List<PluginDesc>) {
    pluginDescList
        .distinctBy { it.module }
        .forEach { plugin(it) }
}

fun PluginDependenciesSpec.plugin(pluginDesc: PluginDesc): PluginDependencySpec {
    val spec = id(pluginDesc.id)
    pluginDesc.version?.also { spec.version(it) }
    return spec
}
