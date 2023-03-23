import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.readTextAsState
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.compose.fontFamilyResource

@Composable
internal fun App() {
    MaterialTheme {
        val initialString: String = stringResource(MR.strings.hello_world)
        var text by remember(initialString) { mutableStateOf(initialString) }
        var counter by remember { mutableStateOf(0) }

        val fileContent: String? by MR.files.some.readTextAsState()
        val assetContent: String? by MR.assets.asset_file.readTextAsState()

        val textColor: Color = colorResource(MR.colors.textColor)

        Column {
            Image(
                modifier = Modifier.size(40.dp),
                painter = painterResource(MR.images.home_black_18),
                contentDescription = null
            )
            Text(
                text = fileContent.orEmpty()
            )
            Text(
                text = assetContent.orEmpty()
            )
            Text(
                text = stringResource(MR.plurals.items_count, counter),
                color = textColor,
            )
            Button(onClick = {
                counter++
                text = "Hello, ${getPlatformName()}"
            }) {
                Text(
                    text = text,
                    fontFamily = fontFamilyResource(MR.fonts.cormorant.italic)
                )
            }
        }
    }
}

expect fun getPlatformName(): String