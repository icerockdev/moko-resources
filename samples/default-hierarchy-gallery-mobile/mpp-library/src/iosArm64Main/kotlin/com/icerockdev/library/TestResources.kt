package com.icerockdev.library

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

public actual val appleStringDesc: StringDesc? = MRappleMain.strings.apple_target_name.desc()

public actual fun appleTargetPlurals(count: Int): StringDesc? {
    return MRappleMain.plurals.apple_targets_count.desc(count)
}
