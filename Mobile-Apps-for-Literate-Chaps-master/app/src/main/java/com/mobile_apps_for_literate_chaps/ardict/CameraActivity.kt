package com.mobile_apps_for_literate_chaps.ardict

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

// Your IDE likely can auto-import these classes, but there are several
// different implementations so we list them here to disambiguate.
import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.graphics.Matrix
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_camera.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import androidx.camera.core.*
import java.io.File
import android.content.Intent
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView

// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CameraActivity : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewFinder = view_finder_id

        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        // EXAMPLES OF SETTING WORD/DEFINITION TEXTVIEW FIELDS
//        val word = findViewById(R.id.wordView) as TextView;
//        word.setText("Ghost");
//
//        val definition = findViewById(R.id.defView) as TextView;
//        definition.setText("The soul of a dead person, a disembodied spirit imagined, " +
//                "usually as a vague, shadowy or evanescent form, as wandering among " +
//                "or haunting living persons.");
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView
    var picturePath = "Not Loaded"

    private fun startCamera() {

        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
        }.build()


        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
                .apply {
                    // We don't set a resolution for image capture; instead, we
                    // select a capture mode which will infer the appropriate
                    // resolution based on aspect ration and requested mode
                    setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                }.build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)
        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {

            val file = File(externalMediaDirs.first(),
                    "CurrentImage.jpg")

            imageCapture.takePicture(file, executor,
                    object : ImageCapture.OnImageSavedListener {
                        override fun onError(
                                imageCaptureError: ImageCapture.ImageCaptureError,
                                message: String,
                                exc: Throwable?
                        ) {
                            val msg = "Photo capture failed: $message"
                            Log.e("CameraXApp", msg, exc)
                            viewFinder.post {
                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onImageSaved(file: File) {
                            val msg = "Photo capture succeeded: ${file.absolutePath}"
                            picturePath = file.absolutePath
                            Log.d("CameraXApp", msg)
//
//                            viewFinder.post {
//                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                            }
                            val intent = Intent(this@CameraActivity, TextRecognitionActivity::class.java)
                            // To pass any data to next activity
                            intent.putExtra("picturePath", picturePath)
                            // start your next activity
                            startActivity(intent)
                        }
                    })
        }

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}
