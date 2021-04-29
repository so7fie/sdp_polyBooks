package com.github.polybooks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.github.polybooks.FillSaleActivity.Companion.pictureBundleK
import kotlinx.android.synthetic.main.activity_take_book_picture.*
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TakeBookPictureFragment : Fragment() {

    private var imageCapture: ImageCapture? = null

    // I think (hope) that using a single fileName is fine as here we are fine with overwriting it everytime a new picture is taken
    // might cause issue in between different isbns
    private val pictureFileName = "bookPictureFile"


    private lateinit var cameraExecutor: ExecutorService

    companion object {
        fun newInstance() = TakeBookPictureFragment()
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.take_book_picture_fragment, container, false)
    }


    override fun onStart() {
        // TODO okay here on start to avoid starting the camera too early, or should be in createView?
        super.onStart()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }


        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto(view: View) {
        // TODO offer flash option and other quality of life upgrades

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return


        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(activity),
            object: ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    // TODO The application is responsible for calling ImageProxy.close() to close the image.
                    super.onCaptureSuccess(image)
                    val msg = "Photo capture succeeded"
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    val bundle = Bundle()
                    bundle.putString(pictureBundleK, pictureFileName)
                    //bundle.putParcelable(pictureBundleK, image) // TODO convert to parcellable, but also as needed by DB
                    // TODO Option 2: abandon this because apparently big sizes are hard to transfer and instead save to cache or user storage and retrieve in next step?
                    // https://stackoverflow.com/questions/4352172/how-do-you-pass-images-bitmaps-between-android-activities-using-bundles
                    // https://stackoverflow.com/questions/2459524/how-can-i-pass-a-bitmap-object-from-one-activity-to-another

                    val buffer: ByteBuffer = image.planes[0].buffer // TODO or image.image ?
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                    saveBitmap(bitmap)


                    parentFragmentManager.setFragmentResult(
                        FillSaleActivity.requestK,
                        bundle
                    )
                }
            })
    }

    fun saveBitmap(bitmap: Bitmap): String {
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val fo: FileOutputStream = requireActivity().openFileOutput(
                pictureFileName,
                Context.MODE_PRIVATE
            )
            fo.write(bytes.toByteArray())
            // remember close file output
            fo.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return pictureFileName
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            // TODO Other option:
            //       = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(activity))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // TODO also improve UX here
                Toast.makeText(
                    activity,
                    "You must grant camera permissions to take a picture.",
                    Toast.LENGTH_SHORT
                ).show()
                // Close the fragment
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    remove(parentFragmentManager.findFragmentById(R.id.fragment_take_picture)!!)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}