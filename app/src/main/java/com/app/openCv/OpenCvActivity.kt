package com.app.openCv

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.app.openCv.OpenCvActivity.Companion.FILENAME_FORMAT
import com.app.openCv.OpenCvActivity.Companion.REQUIRED_PERMISSIONS
import com.app.openCv.OpenCvActivity.Companion.TAG
import com.app.rd.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt


class OpenCvActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        OpenCVLoader.initLocal()

        setContent {

            Surface(color = Color.White, modifier = Modifier) {

                val systemUi = rememberSystemUiController()
                systemUi.setNavigationBarColor(color = Color.Black)

                StartUp(context = this) {
                    if (it) {
                        SimpleCameraPreview()
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "CameraXApp"
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}


@Composable
fun StartUp(context: Context, isGranted: @Composable (Boolean) -> Unit) {

    var isPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
        var permissionGranted = true

        permission.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                permissionGranted = false
            }
        }

        if (!permissionGranted) {
            Toast.makeText(context, "Permission request denied", Toast.LENGTH_SHORT).show()
        } else {
            isPermissionGranted = true
        }
    }

    LaunchedEffect(key1 = Unit) {
        // Request a permission
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    isGranted(isPermissionGranted)

}

@Composable
fun OnLifecycleEvent(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit
) {

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            onEvent.invoke(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun SimpleCameraPreview() {

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var imageBitMap by remember { mutableStateOf<Bitmap?>(null) }
    var savedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(key1 = cameraProviderFuture) {
        imageCapture = ImageCapture.Builder()
            .setFlashMode(ImageCapture.FLASH_MODE_ON)
            .build()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                previewView.scaleType = PreviewView.ScaleType.FILL_START
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()

                        //------------------Image Analyzer---------------
                        val opencvAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setDefaultResolution(android.util.Size(1280, 1080))
                            .build()

                        opencvAnalyzer.setAnalyzer(executor, LightnessAnalyzer(context = context, savedFileListener = {
                            Log.d(TAG, "SimpleCameraPreview: ${it.absoluteFile}")
                        },
                            savedBitmap = {
                                savedBitmap = it
                            }) { bitmap ->
                            imageBitMap = bitmap
                        })

                        //----------------------
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                opencvAnalyzer,
                                imageCapture,
                            ).apply {
                                camera = this
                            }

                        } catch (exc: Exception) {
                            Log.e("OpenCvActivity", "Use case binding failed", exc)
                        }
                    }, executor
                )
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .padding(12.dp)
                .clip(RoundedCornerShape(50))
                .size(75.dp)
                .background(color = Color.White.copy(alpha = 0.8f))
                .align(Alignment.BottomCenter)
                .clickable {
                    imageCapture ?: return@clickable
//                    takePhoto(imageCapture, context)?.let {
//
//                    }
                }
        )

        imageBitMap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .height(100.dp)
                .background(Color.White)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = "Card Scans", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    camera?.let {
                        setFlashIcon(it) { isOn ->
                            isFlashOn = isOn
                        }
                    }
                }) {
                    Image(painter = painterResource(id = if (isFlashOn) R.drawable.flash_on else R.drawable.flash_off), contentDescription = null, colorFilter = ColorFilter.tint(Color.Black))
                }
            }
        }
        savedBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(150.dp, 200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.BottomStart)
            )
        }
    }
}

private fun setFlashIcon(camera: Camera, isFlashOn: (Boolean) -> Unit) {
    if (camera.cameraInfo.hasFlashUnit()) {
        if (camera.cameraInfo.torchState.value == 0) {
            camera.cameraControl.enableTorch(true)
            isFlashOn(true)
        } else {
            camera.cameraControl.enableTorch(false)
            isFlashOn(false)
        }
    } else {
        //hide flag icon for no flash available
    }
}


typealias OpencvListener = (bitmap: Bitmap) -> Unit
typealias SavedFile = (file: File) -> Unit

private class LightnessAnalyzer(context: Context, private val savedFileListener: SavedFile, private val savedBitmap: (Bitmap) -> Unit, private val listener: OpencvListener) : ImageAnalysis.Analyzer {

    private var thresholds = 0
    private var shouldStartCapturing = true
    private val ctx = context

    override fun analyze(image: ImageProxy) {
        image.image?.let { it ->
            if (it.format == ImageFormat.YUV_420_888 && it.planes.size == 3) {
                val frame = it.yuvToRgba()
                val gray = Mat()
                val edges = Mat()

                //Image Processing code goes here!
                Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY)

                Imgproc.adaptiveThreshold(frame, gray, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 13, 5.0)

                // Apply Gaussian blur to the grayscale image 5.0
                Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

                Imgproc.medianBlur(gray, gray, 9)

                // Apply Canny edge detection 75
                Imgproc.Canny(gray, edges, 0.0, 200.0)

                // Apply morphological transformations to close gaps in edges
                val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0))
                Imgproc.morphologyEx(edges, edges, Imgproc.MORPH_CLOSE, kernel)

                // Apply dilation
                Imgproc.dilate(edges, edges, kernel)

                Imgproc.erode(edges, edges, kernel)

                // Find contours
                val contours = mutableListOf<MatOfPoint>()
                val hierarchy = Mat()
                Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

                // Sort contours by area and find the largest one
                contours.sortByDescending { Imgproc.contourArea(it) }

                var documentContour: MatOfPoint? = null

                for (contour in contours) {

                    val area = Imgproc.contourArea(contour)
                    val approx = MatOfPoint2f()
                    Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true) * 0.02, true)
                    val points = approx.toArray()

                    if (area > 5000 && points.size == 4) {

                        if (thresholds < 20 && shouldStartCapturing) {
                            thresholds++
                        }

                        if (thresholds == 20 && shouldStartCapturing) {

                            try {

                                shouldStartCapturing = false

                                val srcPoints = orderPoints(points.toList())

                                val dstPoints = findDest(srcPoints.toList())

                                val dstMat = MatOfPoint2f(*dstPoints.first.toTypedArray())
                                val srcMat = MatOfPoint2f(*srcPoints.toTypedArray())

                                // Perform perspective transform
                                val perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat)
                                val warped = Mat()

                                Imgproc.warpPerspective(frame, warped, perspectiveTransform, Size(dstPoints.second.toDouble(), dstPoints.third.toDouble()), Imgproc.INTER_LINEAR)

                                Core.flip(warped, warped, 1)

                                Log.d(TAG, "onCameraFrame: x = ${srcPoints[0].x}, y = ${srcPoints[0].y}, x = ${srcPoints[1].x} y = ${srcPoints[1].y} x = ${srcPoints[2].x} y = ${srcPoints[2].y} x = ${srcPoints[3].x} y = ${srcPoints[3].y}")

                                val bitmap = Bitmap.createBitmap(warped.cols(), warped.rows(), Bitmap.Config.ARGB_8888)
                                Log.d(TAG, "analyze: ${warped.rows()} + ${warped.cols()}")
                                Utils.matToBitmap(warped, bitmap)

                                // Get cache directory
                                val cacheDir = ctx.cacheDir
                                val file = File(cacheDir, "${System.currentTimeMillis()}.jpg")

                                // Save the image
                                val success = Imgcodecs.imwrite(file.absolutePath, warped)

                                if (success) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        savedFileListener(file)
                                        savedBitmap(bitmap)
                                        delay(3000)
                                        shouldStartCapturing = true
                                    }
                                } else {
                                    shouldStartCapturing = true
                                }
                            } catch (e: Exception) {
                                Log.d(TAG, "saveImg: ${e.message}")
                            }
                        }
                        documentContour = MatOfPoint(*points)
                        break
                    }
                    thresholds /= 2
                }

                documentContour?.let {

                    //If we found a document contour, fill it with the desired color
                    val contoursList = listOf(it)

                    Imgproc.drawContours(frame, contoursList, -1, Scalar(0.0, 0.0, 255.0, 255.0), 2)

                    // Create a mask
                    val mask = Mat.zeros(frame.size(), frame.type())

                    // Fill the mask with the desired color
                    Imgproc.fillPoly(mask, contoursList, Scalar(0.0, 0.0, 255.0, 255.0)) // Opaque red color

                    // Blend the mask with the frame
                    Core.addWeighted(frame, 1.0, mask, 0.5, 0.0, frame) // 0.5 is the transparency factor
                }

                val bmp = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(frame, bmp)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }

                val rotatedBitMap = Bitmap.createBitmap(
                    bmp,
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                listener(rotatedBitMap)
            }
        }
        image.close()
    }
}

fun Image.yuvToRgba(): Mat {
    val rgbaMat = Mat()

    if (format == ImageFormat.YUV_420_888 && planes.size == 3) {
        val chromaPixelStride = planes[1].pixelStride

        if (chromaPixelStride == 2) // chroma channels are interleaved
        {
            assert(planes[0].pixelStride == 1)
            assert(planes[2].pixelStride == 2)
            val yPlane = planes[0].buffer
            val uvPlane1 = planes[1].buffer
            val uvPlane2 = planes[2].buffer

            val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
            val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
            val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
            val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()

            if (addrDiff > 0) {
                assert(addrDiff == 1L)
                Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
            } else {
                assert(addrDiff == -1L)
                Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
            }
        } else // chroma channels are not interleaved
        {
            val yuvBytes = ByteArray(width * (height + height / 2))
            val yPlane = planes[0].buffer
            val uPlane = planes[1].buffer
            val vPlane = planes[2].buffer

            yPlane.get(yuvBytes, 0, width * height)

            val chromaRowStride = planes[1].rowStride
            val chromaRowPadding = chromaRowStride - width / 2

            var offset = width * height

            if (chromaRowPadding == 0) {
                // When the row stride of the chroma channels equals their width, we can copy
                // the entire channels in one go
                uPlane.get(yuvBytes, offset, width * height / 4)
                offset += width * height / 4
                vPlane.get(yuvBytes, offset, width * height / 4)
            } else {
                // When not equal, we need to copy the channels row by row
                for (i in 0 until height / 2) {
                    uPlane.get(yuvBytes, offset, width / 2)
                    offset += width / 2
                    if (i < height / 2 - 1) {
                        uPlane.position(uPlane.position() + chromaRowPadding)
                    }
                }
                for (i in 0 until height / 2) {
                    vPlane.get(yuvBytes, offset, width / 2)
                    offset += width / 2
                    if (i < height / 2 - 1) {
                        vPlane.position(vPlane.position() + chromaRowPadding)
                    }
                }
            }

            val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
            yuvMat.put(0, 0, yuvBytes)
            Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
        }
    }
    return rgbaMat
}

private fun orderPoints(pts: List<Point>): List<Point> {
    val rect = Array(4) { Point() }

    val sum = pts.map { it.x + it.y }
    val diff = pts.map { it.x - it.y }

    // Top-left point will have the smallest sum
    rect[0] = pts[sum.indexOf(sum.minOrNull()!!)]

    // Bottom-right point will have the largest sum
    rect[2] = pts[sum.indexOf(sum.maxOrNull()!!)]

    // Top-right point will have the smallest difference
    rect[1] = pts[diff.indexOf(diff.minOrNull()!!)]

    // Bottom-left point will have the largest difference
    rect[3] = pts[diff.indexOf(diff.maxOrNull()!!)]

    return rect.toList()
}

private fun findDest(srcPoints: List<Point>): Triple<List<Point>, Int, Int> {
    // Calculate the width and height of the detected document in the source image
    val widthA = sqrt((srcPoints[1].x - srcPoints[0].x).pow(2.0) + (srcPoints[1].y - srcPoints[0].y).pow(2.0))
    val widthB = sqrt((srcPoints[2].x - srcPoints[3].x).pow(2.0) + (srcPoints[2].y - srcPoints[3].y).pow(2.0))
    val maxWidth = widthA.coerceAtLeast(widthB).toInt()

    val heightA = sqrt((srcPoints[2].x - srcPoints[1].x).pow(2.0) + (srcPoints[2].y - srcPoints[1].y).pow(2.0))
    val heightB = sqrt((srcPoints[3].x - srcPoints[0].x).pow(2.0) + (srcPoints[3].y - srcPoints[0].y).pow(2.0))
    val maxHeight = heightA.coerceAtLeast(heightB).toInt()

    // Create the destination points array based on the calculated width and height
    val dstPoints = listOf(
        Point(0.0, 0.0),
        Point(maxWidth.toDouble(), 0.0),
        Point(maxWidth.toDouble(), maxHeight.toDouble()),
        Point(0.0, maxHeight.toDouble())
    )

    return Triple(dstPoints, maxWidth, maxHeight)
}

fun takePhoto(imageCapture: ImageCapture?, context: Context): Uri? {

    //Get a stable reference of the modifiable image capture use case
    imageCapture ?: return null
    var imageUri: Uri? = null

    // Create time stamped name and MediaStore entry.
    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/R&D-Image")
        }
    }

    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()


    // Set up image capture listener, which is triggered after photo has
    // been taken
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("OpenCvActivity", "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val msg = "Photo capture succeeded: ${output.savedUri}"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                imageUri = output.savedUri
                Log.d("OpenCvActivity", msg)
            }
        }
    )
    return imageUri
}

