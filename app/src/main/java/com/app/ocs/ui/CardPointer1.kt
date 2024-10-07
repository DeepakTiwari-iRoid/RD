package com.app.ocs.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import com.app.ocs.data.FrontCardResponse
import com.app.ocs.data.TempData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun CardPointer1(
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
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        convertedBitmap?.let { imageBitmap ->
            Canvas(modifier = Modifier.background(color = Color.Green)) {
                drawToBitmap1(
                    imageBitmap = imageBitmap,
                    scope = this,
                    frontCardResponse = TempData.imageProcessedData,
                    oldImgWidth = TempData.imageProcessedData.width,
                    oldImgHeight = TempData.imageProcessedData.height,
                    newImgHeight = 436 * 2,
                    newImgWidth = 370 * 2
                )
            }
        }
    }
}

fun drawToBitmap1(
    imageBitmap: ImageBitmap,
    scope: DrawScope,
    newImgWidth: Int = 960,
    newImgHeight: Int = 1330,
    oldImgWidth: Int = 960,
    oldImgHeight: Int = 1330,
    frontCardResponse: FrontCardResponse,
) {

    Log.d("TAG", "drawToBitmap1: $frontCardResponse")

    scope.apply {
        drawImage(
            imageBitmap,
            dstOffset = IntOffset(0 - newImgWidth / 2, 0 - newImgHeight / 2),
            dstSize = IntSize(newImgWidth, newImgHeight),
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1f) }),
        )


        drawRect(
            topLeft = Offset(0 - newImgWidth / 2f, 0 - newImgHeight / 2f),
            size = androidx.compose.ui.geometry.Size(newImgWidth.toFloat(), newImgHeight.toFloat()),
            color = Color.Black,
            alpha = 0.3f
        )

        val widthRatio = newImgWidth.toFloat() / oldImgWidth
        val heightRatio = newImgHeight.toFloat() / oldImgHeight

        val paths = frontCardResponse.coordinatesList.map { listList ->
            listList.map { listInt ->
                val chunkedList = listInt.map { it.toFloat() }.chunked(2).filter { it.size > 1 }
                chunkedList.map {
                    Offset(
                        x = it[0] * widthRatio - newImgWidth / 2,
                        y = it[1] * heightRatio - newImgHeight / 2
                    )
                }
            }
        }

        paths.map { it ->
            it.forEachIndexed { _, pathPoints ->
                val path = Path().apply {
                    moveTo(pathPoints[0].x, pathPoints[0].y)
                    pathPoints.drop(1).forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    close()
                }
                drawPath(
                    path = path,
                    color = Color.Green,
                    style = Fill
                )
            }
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