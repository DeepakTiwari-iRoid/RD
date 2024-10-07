package com.app.ocs.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.app.ocs.data.ImageData
import com.app.ocs.data.TempData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val coordinates = listOf(
    listOf(289.16f, 974.9f, 287.94f, 981.31f, 287.94f, 981.49f, 294.36f, 980.97f, 294.53f, 972.82f),
    listOf(363.06f, 1093.65f, 366.8f, 1091.78f, 368.47f, 1094.48f, 364.3f, 1098.44f),
    listOf(622.84f, 1048.85f, 623.88f, 1055.92f, 628.67f, 1055.29f, 628.46f, 1047.81f),
    listOf(665.92f, 1009.26f, 663.63f, 1012.8f, 667.58f, 1014.88f, 670.29f, 1012.59f),
    listOf(277.55f, 830.35f, 288.62f, 832.23f, 286.53f, 921.43f, 274.62f, 921.23f),
    listOf(390.33f, 936.69f, 381.05f, 960.65f, 376.56f, 974.43f, 385.54f, 975.63f, 389.74f, 972.93f, 396.03f, 937.59f),
    listOf(297.6f, 1009.6f, 302.62f, 1012.11f, 304.5f, 1016.91f, 301.36f, 1020.04f, 296.77f, 1018.58f, 295.51f, 1015.66f),
    listOf(284.4f, 426.5f, 285.3f, 420.2f, 300.1f, 393.9f, 316.22f, 378.2f, 336.4f, 378.1f, 339.5f, 384.5f, 322f, 387.3f, 307.95f, 403.02f, 298.92f, 410.79f, 300.43f, 424.07f)
)


@Composable
fun CardPointer(
    modifier: Modifier = Modifier
) {
    //alt="psa-1-1909-11-E90-1-American-Caramel-Fred-Tenney-New-York-Giants-PSA-1-MC-r-UUAAOSw-Yphjwjy9"
    val imageUrl = "https://i.ibb.co/27YbvXF/psa-1-1909-11-E90-1-American-Caramel-Fred-Tenney-New-York-Giants-PSA-1-MC-r-UUAAOSw-Yphjwjy9.jpg"

    val ctx = LocalContext.current
    var convertedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(key1 = true) {
        withContext(Dispatchers.IO) {
            val bitmap = getBitmap(ctx, imageUrl)
            convertedBitmap = bitmap.asImageBitmap()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        convertedBitmap?.let { imageBitmap ->
            Canvas(modifier = modifier) {
                drawToBitmap(
                    imageBitmap = imageBitmap,
                    scope = this,
                    coordinates = coordinates,
                )
            }
        }
    }
}

fun drawToBitmap(
    imageBitmap: ImageBitmap,
    scope: DrawScope,
    newImgWidth: Int = 960,
    newImgHeight: Int = 1330,
    oldImgWidth: Int = 960,
    oldImgHeight: Int = 1330,
    coordinates: List<List<Float>>,
) {
    scope.apply {

        drawImage(
            imageBitmap,
            dstOffset = IntOffset(0 - newImgWidth / 2, 0 - newImgHeight / 2),
            dstSize = IntSize(newImgWidth, newImgHeight)
        )

        val widthRatio = newImgWidth.toFloat() / oldImgWidth
        val heightRatio = newImgHeight.toFloat() / oldImgHeight

        val paths = coordinates.map { list ->
            val chunkedList = list.chunked(2).filter { it.size > 1 }
            chunkedList.map {
                Offset(
                    x = it[0] * widthRatio - newImgWidth / 2,
                    y = it[1] * heightRatio - newImgHeight / 2
                )
            }
        }

        paths.forEachIndexed { index, pathPoints ->
            val path = Path().apply {
                moveTo(pathPoints[0].x, pathPoints[0].y)
                pathPoints.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
                close()
            }
            drawPath(
                path = path,
                color = Color.Black,
                style = Fill
            )
        }
    }
}

private suspend fun getBitmap(context: Context, url: String): Bitmap {
    val loading = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .build()
    val result = (loading.execute(request) as SuccessResult).drawable
    return (result as BitmapDrawable).bitmap
}