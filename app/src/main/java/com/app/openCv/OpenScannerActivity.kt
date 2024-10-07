package com.app.openCv

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.rd.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


class OpenScannerActivity : CameraActivity(), CvCameraViewListener2 {

    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    val TAG = "OpenScannerActivity"
    private lateinit var drawView: DrawView
    private lateinit var imgView: ImageView
    private var thresholds = 0
    private var shouldStartCapturing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            Toast.makeText(this, "Initialization failed!", Toast.LENGTH_LONG).show()
            return
        }

        setContentView(R.layout.activity_open_scanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mOpenCvCameraView = findViewById(R.id.activity_java_surface_view)
        drawView = findViewById(R.id.draw_view)
        imgView = findViewById(R.id.imageView)
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE)
        mOpenCvCameraView.disableFpsMeter()
        mOpenCvCameraView.setCameraIndex(0)
        mOpenCvCameraView.setCvCameraViewListener(this)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

        val frame = inputFrame?.rgba() ?: Mat()
        val gray = Mat()
        val edges = Mat()

        // Convert the frame to grayscale
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
                    saveImg(frame, MatOfPoint(*points))
                }

                documentContour = MatOfPoint(*points)
                break
            }

            thresholds = 0
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
        return gray
    }


    private fun saveImg(frame: Mat, points: MatOfPoint) {
        try {

            shouldStartCapturing = false

            val srcPoints = orderPoints(points.toList())

            val dstPoints = findDest(points.toList())

            val srcMat = MatOfPoint2f(*srcPoints.toTypedArray())

            val dstMat = MatOfPoint2f(*dstPoints.toTypedArray())

            val perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat)

            val warped = Mat()
            Imgproc.warpPerspective(frame, warped, perspectiveTransform, Size(dstPoints[2].x, dstPoints[2].y), Imgproc.INTER_LINEAR)

            Log.d(TAG, "onCameraFrame: x = ${srcPoints[0].x}, y = ${srcPoints[0].y}, x = ${srcPoints[1].x} y = ${srcPoints[1].y} x = ${srcPoints[2].x} y = ${srcPoints[2].y} x = ${srcPoints[3].x} y = ${srcPoints[3].y}")

            val bitmap = Bitmap.createBitmap(warped.cols(), warped.rows(), Bitmap.Config.ARGB_8888)

            Utils.matToBitmap(warped, bitmap)

            // Get cache directory
            val cacheDir = cacheDir
            val filename = File(cacheDir, "${System.currentTimeMillis()}.jpg")

            // Save the image
            val success = Imgcodecs.imwrite(filename.absolutePath, warped)

            if (success) {
                CoroutineScope(Dispatchers.Main).launch {
                    imgView.setImageBitmap(bitmap)
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

    private fun findDest(pts: List<Point>): List<Point> {

        val (tl, br, tr, bl) = pts

        // Finding the maximum width.
        val widthA = sqrt((br.x - bl.x).pow(2) + (br.y - bl.y).pow(2))
        val widthB = sqrt((tr.x - tl.x).pow(2) + (tr.y - tl.y).pow(2))
        val maxWidth = max(widthA, widthB)

        // Finding the maximum height.
        val heightA = sqrt((tr.x - br.x).pow(2) + (tr.y - br.y).pow(2))
        val heightB = sqrt((tl.x - bl.x).pow(2) + (tl.y - bl.y).pow(2))
        val maxHeight = max(heightA, heightB)

        //Final destination co-ordinates.
        val destination_corners = listOf(Point(0.0, 0.0), Point(maxWidth, 0.0), Point(maxWidth, maxHeight), Point(0.0, maxHeight))

        return orderPoints(destination_corners)
    }

    public override fun onResume() {
        super.onResume()
        mOpenCvCameraView.enableView()
    }

    public override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return listOf(mOpenCvCameraView)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }

}