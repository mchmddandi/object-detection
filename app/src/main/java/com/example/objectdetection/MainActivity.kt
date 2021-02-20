package com.example.objectdetection

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.objectdetection.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
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
                if (!cameraPermissionAllowed && !readStoragePermissionAllowed && !writeReadStoragePermissionAllowed) {
                    Toast.makeText(this, "Need the permission to use the feature", Toast.LENGTH_SHORT).show()
                }
            }

    private val resultLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
                if (result) {
                    onAnalyzeImage(outputUri)
                }
            }

    private lateinit var canvas: Canvas
    private val paint = Paint()
    private var color: Int? = null

    private val retrofit by lazy {
        Retrofit.getInstance().create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (!checkHasPermission()) {
            requestPermissionLauncher.launch(REQUESTED_PERMISSION)
        }
        binding.btnCapture.setOnClickListener {
            launchNativeCamera()
        }
        color = ResourcesCompat.getColor(resources, R.color.purple_200, theme)
        color?.let {
            paint.color = it
        }
    }


    private fun onAnalyzeImage(uri: Uri?) {
        if (uri == null) return
        val bitmap = getBitmap(uri)
        val scaledImage = getCapturedImage(bitmap)
        Log.d("BITMAP", "${scaledImage.width} : ${scaledImage.height}")
        val encodedBitmap = encodeBitmap(scaledImage)
        val params = DetectionRequest(encodedImage = encodedBitmap)
        lifecycleScope.launch(Dispatchers.IO) {
            val response = retrofit.detectObject(params)
            canvas = Canvas(scaledImage)
            response.detectedObject.forEach {
                canvas.drawRect(
                        it.foodBoundingbox.foodX.toFloat(),
                        it.foodBoundingbox.foodY.toFloat(),
                        (it.foodBoundingbox.foodX + it.foodBoundingbox.foodWidth).toFloat(),
                        (it.foodBoundingbox.foodY + it.foodBoundingbox.foodHeight).toFloat(),
                        paint)
            }
            Log.d("BITMAP", response.toString())
        }
        binding.imgCapture.setImageBitmap(scaledImage)
        binding.imgCapture.invalidate()
    }

    private fun encodeBitmap(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun getCapturedImage(bitmap: Bitmap): Bitmap {
        // Crop image to match imageView's aspect ratio
        val scaleFactor = Math.min(
                bitmap.width / binding.imgCapture.width.toFloat(),
                bitmap.height / binding.imgCapture.height.toFloat()
        )
//
        val deltaWidth = (bitmap.width - binding.imgCapture.width * scaleFactor).toInt()
        val deltaHeight = (bitmap.height - binding.imgCapture.height * scaleFactor).toInt()

        val scaledImage = Bitmap.createBitmap(
                bitmap,
                deltaWidth / 2,
                deltaHeight / 2,
                bitmap.width - deltaWidth,
                bitmap.height - deltaHeight
        )
        bitmap.recycle()
        return scaledImage
    }

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
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    .let {
                        outputUri = it
                    }
        } else {
            val tempFile = createTempFileImage()
            outputUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID.plus(".provider"),
                    tempFile
            )
        }
    }

    private fun createTempFileImage(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val imageFileName = timeStamp.toString() + "_"
        val fileDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
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
    }
}

