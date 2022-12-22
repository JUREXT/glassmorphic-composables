package dev.jakhongirmadaminov.glassmorphic_sample

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jakhongirmadaminov.glassmorphic_sample.ui.theme.MyApplicationTheme
import dev.jakhongirmadaminov.glassmorphic_sample.ui.componets.GlassMorphicColumn
import dev.jakhongirmadaminov.glassmorphic_sample.ui.componets.Place
import dev.jakhongirmadaminov.glassmorphic_sample.util.fastblur
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

const val BLURRED_BG_KEY = "BLURRED_BG_KEY"
const val BLUR_RADIUS = 50

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Sample()
                    }
                }
            }
        }
    }
}

@Composable
fun Sample() {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val cardWidthDp = screenWidthDp / 2
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(App.getInstance().memoryCache[BLURRED_BG_KEY]) }

    val scrollState = rememberScrollState()
    val items = arrayListOf<Int>()
    for (i in 0 until 20) {
        items.add(i)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val captureController = rememberCaptureController()
        Capturable(
            controller = captureController,
            onCaptured = { bitmap, _ ->
                // This is captured bitmap of a content inside Capturable Composable.
                bitmap?.let {
                    fastblur(it.asAndroidBitmap(), 1f, BLUR_RADIUS)?.let { fastBlurred ->
                        // Bitmap is captured successfully. Do something with it!
                        App.getInstance().memoryCache.put(BLURRED_BG_KEY, fastBlurred)
                        capturedBitmap = fastBlurred
                    }
                }
            }

        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_autumn),
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        LaunchedEffect(key1 = true, block = {
            withContext(Main) {
                if (capturedBitmap == null) captureController.capture()
            }
        })

        val childMeasures = remember { items.map { Place() }.toImmutableList() }

        capturedBitmap?.let { capturedImage ->
            GlassMorphicColumn(
                modifier = Modifier.padding(start = 0.dp),
                scrollState = scrollState,
                childMeasures = childMeasures,
                targetBitmap = capturedImage.asImageBitmap(),
                dividerSpace = 10,
                blurRadius = BLUR_RADIUS,
                drawOnTop = { path ->
                    val strokeColor = Color(0x80ffffff)
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(1f),
                    )
                },
                content = {
                    items.forEachIndexed { index, it ->
                        Box(
                            modifier = Modifier
                                .onGloballyPositioned {
                                    childMeasures[index].apply {
                                        sizeX = it.size.width
                                        sizeY = it.size.height
                                        offsetX = it.positionInParent().x
                                        offsetY = it.positionInParent().y
                                    }
                                }
                                .width(cardWidthDp.dp)
                                .padding(15.dp)
                        ) {
                            Text("Item $it", color = Color.White)
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Sample()
        }
    }
}