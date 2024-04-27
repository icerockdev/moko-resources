/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.UIKit.UIColor
import platform.UIKit.colorNamed

fun ColorResource.getUIColor(): UIColor {
    val color: UIColor? = UIColor.colorNamed(name = this.name)
    return requireNotNull(color) {
        "Can't read color $name, please check moko-resources gradle configuration"
    }
}
