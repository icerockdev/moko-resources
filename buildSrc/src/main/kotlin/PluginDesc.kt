/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.plugin.use.PluginDependenciesSpec

data class PluginDesc(
    val id: String,
    val module: String
)

fun DependencyHandlerScope.plugin(pluginDesc: PluginDesc) {
    "classpath"(pluginDesc.module)
}

fun DependencyHandlerScope.plugins(pluginDescList: List<PluginDesc>) {
    pluginDescList
        .distinctBy { it.module }
        .forEach { plugin(it) }
}

fun PluginDependenciesSpec.plugin(pluginDesc: PluginDesc) {
    id(pluginDesc.id)
}
