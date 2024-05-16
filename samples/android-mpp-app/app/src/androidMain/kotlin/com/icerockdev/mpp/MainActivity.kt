/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.mpp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.icerockdev.library.TestRes

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TestRes.test()
    }
}
