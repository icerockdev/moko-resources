import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.icerockdev.library.MR
import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.readTextAsState
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun App() {
    MaterialTheme {
        val initialString: String = stringResource(MR.strings.hello_world)
        var text by remember(initialString) { mutableStateOf(initialString) }
        var counter by remember { mutableStateOf(0) }

        val fileContent: String by MR.files.some.readTextAsState()
        val assetContent: String by MR.assets.asset_file.readTextAsState()

        val textColor: Color = colorResource(MR.colors.textColor)

        Column {
            // TODO will be added in https://github.com/icerockdev/moko-resources/issues/400
//            Image(
//                painter = imageResource(MR.images.home_black_18),
//                contentDescription = null
//            )
            Text(
                text = fileContent
            )
            Text(
                text = assetContent
            )
            Text(
                text = stringResource(MR.plurals.items_count, counter),
                color = textColor,
            )
            Button(onClick = {
                counter++
                text = "Hello, ${getPlatformName()}"
            }) {
                // TODO will be added in https://github.com/icerockdev/moko-resources/issues/440
//                val font: FontFamily = fontResource(MR.fonts.cormorant.italic)
                Text(
                    text = text,
//                    fontFamily = font
                )
            }
        }
    }
}

expect fun getPlatformName(): String