/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.icerockdev.library.Testing

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val imageView: ImageView = findViewById(R.id.imageView)
        val textView: TextView = findViewById(R.id.textView)
        val stringDescTextView: TextView = findViewById(R.id.stringDescTextView)

        val strings = Testing.getStrings()
        val text = strings.joinToString("\n") { it.toString(context = this) }

        val drawable = Testing.getDrawable()

        imageView.setImageResource(drawable.drawableResId)
        textView.text = text
        textView.typeface = Testing.getFont1().getTypeface(context = this)

        stringDescTextView.text = Testing.getStringDesc().toString(context = this)
        stringDescTextView.typeface = Testing.getFont2().getTypeface(context = this)
    }

    /**
     * For unit-tests purposes.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHandler.updateLocale(newBase))
    }
}
