/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.mpp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.icerockdev.library.MR
import com.icerockdev.library.MR.strings
import com.icerockdev.library.MRandroidMain
import com.icerockdev.library.android_name
import com.icerockdev.library.common_name

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println(MR.strings.common_name)
        println(MRandroidMain.strings.android_name)
    }
}
