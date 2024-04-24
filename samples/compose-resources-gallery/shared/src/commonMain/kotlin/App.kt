import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icerockdev.library.MR
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.readTextAsState
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun App() {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colors.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(MR.images.moko_logo),
                contentDescription = null
            )

            Image(
                modifier = Modifier.size(30.dp).padding(top = 16.dp),
                painter = painterResource(MR.images.car_black),
                contentDescription = null,
            )

            var text: String by remember { mutableStateOf("") }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 16.dp),
                value = text,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onBackground
                ),
                onValueChange = { text = it }
            )

            val counter: Int = text.length
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                text = stringResource(MR.plurals.chars_count, counter, counter),
                color = colorResource(MR.colors.textColor),
                fontFamily = fontFamilyResource(MR.fonts.cormorant_italic)
            )

            Button(onClick = { text = "Hello, ${getPlatformName()}" }) {
                Text(text = stringResource(MR.strings.hello_world))
            }

            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(MR.strings.new_line_test),
                color = MaterialTheme.colors.onBackground,
            )

            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(MR.strings.symbols_text),
                color = MaterialTheme.colors.onBackground,
            )

            val fileContent: String? by MR.files.some_file_txt.readTextAsState()
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = fileContent.orEmpty(),
                color = MaterialTheme.colors.onBackground
            )

            val assetContent: String? by MR.assets.some_asset_txt.readTextAsState()
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = assetContent.orEmpty(),
                color = MaterialTheme.colors.onBackground
            )
            val assetContent2: String? by MR.assets.additionalDir.second_inner_text_file_txt.readTextAsState()
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = assetContent2.orEmpty(),
                color = MaterialTheme.colors.onBackground
            )
            val assetContent3: String? by MR.assets.additionalDir.innerDir.innerText_txt.readTextAsState()
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = assetContent3.orEmpty(),
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}

expect fun getPlatformName(): String
