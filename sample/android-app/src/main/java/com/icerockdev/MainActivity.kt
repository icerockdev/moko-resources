/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.icerockdev.library.Testing
import com.icerockdev.library.MR
import dev.icerock.moko.graphics.colorInt
import dev.icerock.moko.resources.getColor

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

        val textColor = MR.colors.textColor.getColor(this).colorInt()
            .run(Integer::toHexString)
            .let { "#$it" }
            .run(Color::parseColor)
        textView.setTextColor(textColor)

        stringDescTextView.text = Testing.getStringDesc().toString(context = this)
        stringDescTextView.typeface = Testing.getFont2().getTypeface(context = this)

        listOf(
            Testing.getTextFile().readText(context = this),
            Testing.getJsonFile().readText(context = this),
            Testing.getNestedJsonFile().readText(context = this)
        ).forEach { Log.d(MainActivity::class.java.simpleName, it) }
    }

    /**
     * For unit-tests purposes.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHandler.updateLocale(newBase))
    }
}
