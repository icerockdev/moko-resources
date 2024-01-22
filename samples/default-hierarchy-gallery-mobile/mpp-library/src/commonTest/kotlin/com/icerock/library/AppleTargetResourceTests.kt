package com.icerock.library

import com.icerockdev.library.appleStringDesc
import com.icerockdev.library.appleTargetPlurals
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test

class AppleTargetResourceTests : BaseStringResourceTests("en") {

    @Test
    fun checkAppleTargetString() {
        val actualString: StringDesc = appleStringDesc ?: return

        stringTest(
            expected = "Apple Target String",
            actual = actualString
        )
    }

    @Test
    fun compareApplePlurals0() {
        val pluralString: StringDesc = appleTargetPlurals(0) ?: return

        pluralTest(
            expected = "No targets",
            actual = pluralString
        )
    }

    @Test
    fun compareApplePlurals1() {
        val pluralString: StringDesc = appleTargetPlurals(1) ?: return

        pluralTest(
            expected = "One target",
            actual = pluralString
        )
    }

    @Test
    fun compareApplePlurals5() {
        val actualString: StringDesc = appleTargetPlurals(5) ?: return

        pluralTest(
            expected = "Same targets",
            actual = actualString
        )
    }
}
