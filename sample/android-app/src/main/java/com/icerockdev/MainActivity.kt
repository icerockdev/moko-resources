package com.icerockdev

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

        val strings = Testing.getStrings()
        val text = strings.joinToString("\n") { it.toString(context = this) }

        val drawable = Testing.getDrawable()

        imageView.setImageResource(drawable.drawableResId)
        textView.text = text
    }
}
