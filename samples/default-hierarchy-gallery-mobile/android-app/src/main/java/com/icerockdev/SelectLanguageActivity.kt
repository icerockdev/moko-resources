/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.icerockdev.library.Testing

class SelectLanguageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_language)
    }

    fun onSystemLanguage(view: View) {
        Testing.locale(null)

        startActivity(Intent(this, MainActivity::class.java))
    }

    fun onCustomLanguage(view: View) {
        view as Button

        Testing.locale(view.text.toString())

        startActivity(Intent(this, MainActivity::class.java))
    }
}
