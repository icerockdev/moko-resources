/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.icerockdev.library.Testing
import dev.icerock.moko.graphics.colorInt
import dev.icerock.moko.resources.AssetResource
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
        imageView.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            Testing.getGradientColors().map {
                it.getColor(context = this).colorInt()
            }.toIntArray()
        )

        textView.text = text
        textView.typeface = Testing.getFont1().getTypeface(context = this)

        val textColorFromRes = ContextCompat.getColor(this, R.color.textColor)
        textView.setTextColor(textColorFromRes)

        stringDescTextView.text = Testing.getStringDesc().toString(context = this)
        stringDescTextView.typeface = Typeface.createFromAsset(assets, Testing.getFontAssetsPath())//Testing.getFont2().getTypeface(context = this)

        val textColorFromLib = Testing.getTextColor().getColor(context = this).colorInt()
        stringDescTextView.setTextColor(textColorFromLib)

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
