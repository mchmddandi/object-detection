package com.example.objectdetection

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.objectdetection.databinding.ActivityMainBinding
import com.example.objectdetection.ml.SsdMobilenetV11Metadata1
import com.example.objectdetection.ml.Yolov4416
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var model: Yolov4416
    private lateinit var binding: ActivityMainBinding
    private var outputUri: Uri? = null
    private var cameraPermissionAllowed = false
    private var readStoragePermissionAllowed = false
    private var writeReadStoragePermissionAllowed = false
    private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                cameraPermissionAllowed = permissions[Manifest.permission.CAMERA] ?: false
                readStoragePermissionAllowed =
                        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
                writeReadStoragePermissionAllowed =
                        permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
                if (cameraPermissionAllowed && readStoragePermissionAllowed && writeReadStoragePermissionAllowed) {
                    launchNativeCamera()
                }
            }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            onAnalyzeImage(outputUri)
        }
    }

    private var imgData: ByteBuffer = ByteBuffer.allocateDirect(
            1       // DIM_BATCH_SIZE
                    * 416   // Input image width
                    * 416   // Input image height
                    * 3     // Pixel size
                    * 4)    // Bytes per channel

    private val intValues = IntArray(416 * 416)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        model = Yolov4416.newInstance(this)
        imgData.order(ByteOrder.nativeOrder())
        if (!checkHasPermission()) {
            requestPermissionLauncher.launch(REQUESTED_PERMISSION)
        }
        binding.btnCapture.setOnClickListener {
            launchNativeCamera()
        }

    }
    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imgData.rewind()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 416, 416, true)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        for (i in 0 until 416) {
            for (j in 0 until 416) {
                val value = intValues[pixel++]
                addPixelValue(value)
            }
        }
    }

    private fun addPixelValue(pixelValue: Int) {
        imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
        imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
        imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
    }

    private fun onAnalyzeImage(uri: Uri?) {
        if (uri == null) return
        val bitmap = getBitmap(uri)
        val scaledImage = getCapturedImage(bitmap)
        convertBitmapToByteBuffer(scaledImage)
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 416, 416, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(imgData)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val outputFeature1 = outputs.outputFeature1AsTensorBuffer
//        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 416, 416, 3), DataType.FLOAT32)
//        inputFeature0.loadBuffer(image.buffer)
//        val output = model.process(inputFeature0)
//        val outputFeature0 = output.outputFeature0AsTensorBuffer
//        val outputFeature1 = output.outputFeature1AsTensorBuffer
        Log.d("OUTPUT", outputFeature0.shape.toString())
//        Log.d("OUTPUT", inputFeature0.toString())
        binding.imgCapture.setImageBitmap(scaledImage)
    }

    private fun getCapturedImage(bitmap: Bitmap): Bitmap {
        // Crop image to match imageView's aspect ratio
//        val scaleFactor = Math.min(
//                416 / bitmap.width.toFloat(),
//                416 / bitmap.height.toFloat()
//        )
//
//        val deltaWidth = (bitmap.width * scaleFactor).toInt()
//        val deltaHeight = (bitmap.height * scaleFactor).toInt()

        val scaledImage = Bitmap.createScaledBitmap(
                bitmap,
                416,
                416,
                true
        )
//        Bitmap(
//                bitmap, deltaWidth / 2, deltaHeight / 2,
//                bitmap.width - deltaWidth, bitmap.height - deltaHeight
//        )
        bitmap.recycle()
        return scaledImage.copy(Bitmap.Config.ARGB_8888, true)
    }

//    private fun resizeImage(bitmap: Bitmap): Bitmap {
//        val ratio = Math.min(
//                416 / bitmap.width,
//                416 / bitmap.height
//        )
//        val width = Math.round((ratio * bitmap.width)).toInt()
//        val height = Math.round((ratio * bitmap.height)).toInt()
//
//        return Bitmap.createScaledBitmap(
//                bitmap, width,
//                height, true
//        )
//    }

    private fun getBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    private fun launchNativeCamera() {
        createTempFile()
        resultLauncher.launch(outputUri)
    }

    private fun createTempFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = createContentValuesImage()
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).let {
                outputUri = it
            }
        } else {
            val tempFile = createTempFileImage()
            outputUri = FileProvider.getUriForFile(
                    this,
                    MainActivity::class.java.simpleName.plus(".provider"),
                    tempFile
            )
        }
    }

    private fun createTempFileImage(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val imageFileName = timeStamp.toString() + "_"
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName,
                ".jpg",
                fileDirectory
        )
    }

    private fun createContentValuesImage(): ContentValues {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val imageFileName = timeStamp.toString() + "_"
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
    }

    private fun checkHasPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
                || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        val REQUESTED_PERMISSION = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        const val REQUEST_CODE_IMAGE_CAPTURE = 10
        private const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128.0f
    }
}

