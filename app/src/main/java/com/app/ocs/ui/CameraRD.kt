package com.app.ocs.ui

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.app.ocs.OCSActivity.Companion.FILE_FORMAT
import com.app.ocs.OCSViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CameraRD(
    viewModel: OCSViewModel
) {

    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPreviewing by remember { mutableStateOf(false) }

    val cameraController = LifecycleCameraController(ctx)
    cameraController.bindToLifecycle(lifecycleOwner)

    Box(modifier = Modifier.fillMaxSize()) {

        if (isPreviewing && capturedImageUri != null) {

            ImagePreview(
                itemCount = viewModel.uriList.size,
                capturedImageUri!!,
                onSave = {
                    saveImageToGallery(ctx, capturedImageUri!!)
                    viewModel.updateList(capturedImageUri!!.toString())
                    isPreviewing = false
                    capturedImageUri = null
                }, onDiscard = {
                    deleteCachedImage(ctx, capturedImageUri!!)
                    isPreviewing = false
                    capturedImageUri = null
                },
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = {
                    isPreviewing = false
                    capturedImageUri = null
                },
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopStart)
            ) {
                Image(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .background(Color.White)
                )
            }

        } else {

            AndroidView(
                factory = {
                    PreviewView(it).apply {
                        controller = cameraController
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(120.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = Color.LightGray
            ) {
                Button(
                    onClick = {
                        takePhoto(ctx, cameraController) {
                            capturedImageUri = it
                            isPreviewing = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text(text = "Take Photo", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun ImagePreview(
    itemCount: Int,
    uri: Uri,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(modifier = modifier) {

        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .align(Alignment.BottomCenter)
        ) {

            OutlinedButton(onClick = onDiscard) {
                Text(text = "Re-take")
            }

            Button(onClick = onSave) {
                Text(text = if (itemCount > 0) "Save" else "Next")
//                Text(text = pluralStringResource(id = R.plurals.destinationName, count = itemCount))
            }

        }
    }
}

fun takePhoto(context: Context, cameraController: LifecycleCameraController, onImageCaptured: (Uri) -> Unit) {

    val outputDirectory = context.externalCacheDirs.firstOrNull() ?: context.filesDir
    val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    cameraController.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraRD", "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}

fun saveImageToGallery(context: Context, imageUri: Uri) {
    val name = SimpleDateFormat(FILE_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraRD-Images")
        }
    }

    val inputStream = context.contentResolver.openInputStream(imageUri)
    val contentResolver = context.contentResolver
    val newUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    newUri?.let { uri ->
        val outputStream = contentResolver.openOutputStream(uri)
        inputStream?.copyTo(outputStream!!)
        outputStream?.close()
        inputStream?.close()
    }
    Toast.makeText(context, "Photo saved to gallery", Toast.LENGTH_LONG).show()
}


fun deleteCachedImage(context: Context, uri: Uri) {
    try {

        val file = File(uri.path ?: "")

        if (file.exists()) {
            file.delete()
            Toast.makeText(context, "Cached image deleted", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("CameraRD", "Failed to delete cached image: ${e.message}", e)
    }
}
