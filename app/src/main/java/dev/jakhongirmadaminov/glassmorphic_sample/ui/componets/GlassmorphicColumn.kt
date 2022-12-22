package dev.jakhongirmadaminov.glassmorphic_sample.ui.componets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import dev.jakhongirmadaminov.glassmorphic_sample.util.fastblur
import kotlinx.collections.immutable.ImmutableList

@Composable
fun GlassMorphicColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    childMeasures: ImmutableList<Place>,
    targetBitmap: ImageBitmap,
    isAlreadyBlurred: Boolean = false,// providing already blurred bitmap consumes less resources
    dividerSpace: Int = 10,
    blurRadius: Int = 100,
    childCornerRadius: Int = 10,
    drawOnTop: DrawScope.(Path) -> Unit = {},
    content: @Composable (ColumnScope.() -> Unit),
) {

    if (childMeasures.isEmpty()) return
    val blurredBg = remember {
        if (isAlreadyBlurred) {
            targetBitmap
        } else {
            fastblur(targetBitmap.asAndroidBitmap(), 1f, blurRadius)?.asImageBitmap() ?: return
        }
    }

    var containerMeasures by remember { mutableStateOf(Place()) }
    val calculatedWidth = containerMeasures.sizeX.dp.let { parentDp ->
        containerMeasures.offsetX.toInt().dp.let { childDp ->
            parentDp + childDp
        }
    }

    Canvas(
        modifier = modifier
            .verticalScroll(scrollState)
            .width(calculatedWidth)
            .height(containerMeasures.sizeY.dp)
    ) {
        for (i in childMeasures.indices) {
            val path = Path()
            path.addRoundRect(
                RoundRect(
                    Rect(
                        offset = Offset(
                            childMeasures[i].offsetX,
                            childMeasures[i].offsetY
                        ),
                        size = Size(
                            childMeasures[i].sizeX.toFloat(),
                            childMeasures[i].sizeY.toFloat()
                        ),
                    ),
                    CornerRadius(childCornerRadius.dp.toPx())
                )
            )

            clipPath(path) {
                drawImage(
                    blurredBg,
                    Offset(
                        -containerMeasures.offsetX,
                        scrollState.value.toFloat() - containerMeasures.offsetY
                    )
                )
            }
            drawOnTop(path)
        }
    }

    Box(modifier = modifier
        .fillMaxSize()
        .clickable(indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
        })

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .onGloballyPositioned {
                if (containerMeasures.sizeX == 0 && containerMeasures.sizeY == 0) {
                    containerMeasures = Place(
                        it.size.width,
                        it.size.height,
                        it.positionInParent().x,
                        it.positionInParent().y
                    )
                }
            },
        verticalArrangement = Arrangement.spacedBy(dividerSpace.dp),
    ) {
        content()
    }
}