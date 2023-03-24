package me.amikhailov.common

import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.icerockdev.app.MR

@Composable
fun App() {
    var text by remember {
        mutableStateOf(MR.strings.hello_world.localized())
    }

    Button(onClick = {
        text = "Hello, ${getPlatformName()}"
    }) {
        Text(text)
    }
}
