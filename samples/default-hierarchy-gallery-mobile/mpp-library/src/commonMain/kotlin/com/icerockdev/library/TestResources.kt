package com.icerockdev.library

import dev.icerock.moko.resources.desc.StringDesc

public expect val appleStringDesc: StringDesc?

public expect fun appleTargetPlurals(count: Int): StringDesc?
