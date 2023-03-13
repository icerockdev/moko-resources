import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.icerockdev.library.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun App() {
    MaterialTheme {
        val initialString: String = stringResource(MR.strings.hello_world)
        var text by remember(initialString) { mutableStateOf(initialString) }

        Button(onClick = {
            text = "Hello, ${getPlatformName()}"
        }) {
            Text(text)
        }
    }
}

expect fun getPlatformName(): String