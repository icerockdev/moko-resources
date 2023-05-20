/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.UIKit.UIColor
import platform.UIKit.colorNamed

fun ColorResource.getUIColor(): UIColor {
    val color: UIColor? = UIColor.colorNamed(
        name = this.name,
        inBundle = this.bundle,
        compatibleWithTraitCollection = null
    )
    return requireNotNull(color) {
        "Can't read color $name from bundle $bundle, please check moko-resources gradle configuration"
    }
}
