package com.app.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StampedPathEffectStyle
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import com.app.rd.ui.theme.gradientColors

@Preview(showBackground = true)
@Composable
fun ComponentTest1() {

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val initYOffset = 100f

        val borderPath = Path().apply {
            moveTo(100f, 100f)
            lineTo(size.width - 100f, 100f)
            lineTo(size.width - 100f, size.height - 100f)
            lineTo(100f, size.height - 100f)
            close()
        }

        drawPath(
            path = borderPath,
            color = Color.Blue,
            style = Stroke(width = 15f, join = StrokeJoin.Round)
        )

        drawLine(
            start = Offset(x = 100f, y = initYOffset),
            end = Offset(x = size.width - 100f, y = initYOffset),
            color = Color.Black
        )

        drawLine(
            brush = Brush.sweepGradient(gradientColors),
            start = Offset(x = 100f, y = initYOffset + 60f),
            end = Offset(x = size.width - 100f, y = initYOffset + 60f),
            strokeWidth = 50f
        )

        /*     drawLine(
                 pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 50f)),
                 start = Offset(x = 100f, y = 70f),
                 end = Offset(x = 100f, y = size.height),
                 color = Color.Blue,
                 cap = StrokeCap.Round,
                 strokeWidth = 10f
             )
     */
        val path = Path().apply {
            moveTo(10f, 0f)
            lineTo(20f, 10f)
            lineTo(10f, 20f)
            lineTo(0f, 10f)
        }

        drawLine(
            pathEffect = PathEffect.stampedPathEffect(
                shape = path,
                advance = 30f,
                phase = 30f,
                style = StampedPathEffectStyle.Rotate
            ),
            start = Offset(x = 100f, y = initYOffset + 150),
            end = Offset(x = size.width - 100f, y = initYOffset + 150),
            color = Color.Green,
            strokeWidth = 10f
        )

        val canvasWidth = size.width / 2
        val canvasHeight = size.height / 2
        val radius = canvasHeight / 6
        val space = (canvasWidth - 4 * radius) / canvasWidth + 100

        with(drawContext.canvas.nativeCanvas) {
            val checkPoint = saveLayer(null, null)

            drawCircle(
                color = Color.Red,
                radius = radius,
                center = Offset(space + radius + 50f, canvasHeight / 2),
            )

            drawCircle(
                blendMode = BlendMode.DstOut,
                color = Color.Blue,
                radius = radius,
                center = Offset(space + 3 * radius - 50f, canvasHeight / 2),
            )

            restoreToCount(checkPoint)
        }


        val rectHeight = canvasHeight / 2
        val rectWidth = (canvasWidth - 4 * space) / 3

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Green,
                    Color.Red,
                    Color.Blue,
                    Color.Yellow,
                    Color.Magenta
                ),
                center = Offset(canvasWidth / 2 - 25, canvasHeight - 200),
                tileMode = TileMode.Mirror,
                radius = 10f
            ),
            topLeft = Offset(110f, canvasHeight / 2 + 250),
            size = Size(canvasWidth / 2, canvasHeight / 5)
        )

    }
}


@Preview(showBackground = true)
@Composable
fun ComponentTest2() {
    Canvas(modifier = Modifier.fillMaxSize()) {

    }
}