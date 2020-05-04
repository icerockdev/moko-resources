/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context
import android.content.res.Resources
import android.os.Build

object Utils {
    fun processArgs(args: List<Any>, context: Context): Array<out Any> {
        return args.toList().map { (it as? StringDesc)?.toString(context) ?: it }.toTypedArray()
    }

    fun resourcesForContext(context: Context): Resources {
        return localizedContext(context).resources
    }

    private fun localizedContext(context: Context): Context {
        if (StringDesc.localeType.systemLocale == null) return context

        val resources = context.resources
        val config = resources.configuration

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(StringDesc.localeType.systemLocale)
            context.createConfigurationContext(config)
        } else {
            config.locale = StringDesc.localeType.systemLocale
            resources.updateConfiguration(config, resources.displayMetrics)
            context
        }
    }
}
