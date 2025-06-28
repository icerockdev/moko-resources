/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.icerockdev.library.Testing
import com.icerockdev.library.R as libraryR

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val imageView: ImageView = findViewById(R.id.imageView)!!
        val svgImageView: ImageView = findViewById(R.id.svgImageView)!!
        val textView: TextView = findViewById(R.id.textView)!!
        val stringDescTextView: TextView = findViewById(R.id.stringDescTextView)!!
        val otfFontsTestTextView: TextView = findViewById(R.id.stringOtfFontsTest)!!

        val strings = Testing.getStrings()
        val text = strings.joinToString("\n") { it.toString(context = this) }

        val drawable = Testing.getDrawable()

        imageView.setImageResource(drawable.drawableResId)
        imageView.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            Testing.getGradientColors().map {
                it.getColor(context = this)
            }.toIntArray()
        )

        val vectorDrawable = Testing.getVectorDrawable()
        svgImageView.setImageResource(vectorDrawable.drawableResId)

        textView.text = text
        textView.typeface = Testing.getFontTtf1().getTypeface(context = this)

        val textColorFromRes = ContextCompat.getColor(this, libraryR.color.textColor)
        textView.setTextColor(textColorFromRes)

        stringDescTextView.text = Testing.getStringDesc().toString(context = this)
        stringDescTextView.typeface = Testing.getFontTtf2().getTypeface(context = this)

        val textColorFromLib = Testing.getTextColor().getColor(context = this)
        stringDescTextView.setTextColor(textColorFromLib)

        listOf(
            Testing.getTextFile().readText(context = this),
            Testing.getJsonFile().readText(context = this),
            Testing.getNestedJsonFile().readText(context = this),
            *Testing.getTextsFromAssets().map { it.readText(context = this) }.toTypedArray()
        ).forEach { Log.d(MainActivity::class.java.simpleName, it) }

        otfFontsTestTextView.typeface = Testing.getFontOtf1().getTypeface(context = this)
        otfFontsTestTextView.typeface = Testing.getFontOtf2().getTypeface(context = this)
        otfFontsTestTextView.typeface = Testing.getFontOtf3().getTypeface(context = this)
    }

    /**
     * For unit-tests purposes.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHandler.updateLocale(newBase))
    }
}
