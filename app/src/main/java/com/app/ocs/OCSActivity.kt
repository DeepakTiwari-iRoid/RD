package com.app.ocs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.app.ocs.ui.magnification.CardMagnificationScreen
import com.app.ocs.ui.theme.RDTheme


class OCSActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RDTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {

                    /*CardPointer1(
                        modifier = Modifier
                            .fillMaxSize()
                    )*/

                    CardMagnificationScreen()

                }
            }

        }
    }


    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            var permissionGranted = true

            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSION && !it.value) {
                    permissionGranted = false
                }
            }

            if (!permissionGranted) {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            } else {
                /*setCameraPreview()*/
            }
        }

    companion object {
        const val FILE_FORMAT = "yyyy-mm-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSION =
            mutableListOf(Manifest.permission.CAMERA).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun hasPermissions(context: Context) = REQUIRED_PERMISSION.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    }
}
