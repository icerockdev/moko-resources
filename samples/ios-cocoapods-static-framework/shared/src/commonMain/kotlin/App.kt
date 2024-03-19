import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.share.resources.module.MR
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun App() {
    MaterialTheme {
        var showImage by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {

            Button(
                onClick = remember {
                    {
                        showImage = !showImage
                    }
                }
            ) {
                Text(
                    text = if (!showImage) {
                        stringResource(MR.strings.hello_world)
                    } else {
                        "Hello, ${getPlatformName()}"
                    }
                )
            }

            AnimatedVisibility(showImage) {
                Image(
                    painter = painterResource(MR.images.action_back_white),
                    contentDescription = null
                )
            }
        }
    }
}

expect fun getPlatformName(): String