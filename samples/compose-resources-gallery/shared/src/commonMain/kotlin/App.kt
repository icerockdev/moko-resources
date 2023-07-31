import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.icerockdev.library.MR
import dev.icerock.moko.resources.compose.*

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
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground)
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
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 8.dp),
                text = stringResource(MR.plurals.chars_count, counter, counter),
                color = colorResource(MR.colors.textColor),
                fontFamily = fontFamilyResource(MR.fonts.cormorant.italic)
            )

            Button(onClick = { text = "Hello, ${getPlatformName()}" }) {
                Text(text = stringResource(MR.strings.hello_world))
            }

            val fileContent: String? by MR.files.some_file.readTextAsState()
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = fileContent.orEmpty(),
                color = MaterialTheme.colors.onBackground
            )

            val assetContent: String? by MR.assets.some_asset.readTextAsState()
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = assetContent.orEmpty(),
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}

expect fun getPlatformName(): String
