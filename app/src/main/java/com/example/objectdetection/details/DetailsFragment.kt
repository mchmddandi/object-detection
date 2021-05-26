package com.example.objectdetection.details

import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.objectdetection.*
import com.example.objectdetection.databinding.FragmentDetailsBinding
import com.example.objectdetection.details.nutrition.NutritionFragment
import com.example.objectdetection.details.nutrition.NutritionsPagerAdapter
import com.example.objectdetection.home.recentactivity.RecentActivity
import com.example.objectdetection.home.recentactivity.RecentActivityRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DetailsFragment : Fragment() {
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var canvas: Canvas
    private val boxPaint = Paint()
    private val textPaint = Paint()
    private var color: Int? = null

    private val retrofit by lazy {
        Retrofit.getInstance().create(ApiService::class.java)
    }

    private val repo by lazy {
        RecentActivityRepositoryImpl(Firestore.getInstance())
    }

    private val navController by lazy {
        findNavController()
    }

    private lateinit var vpNutritionsPagerAdapter: NutritionsPagerAdapter
    private val listOfNutritionFragments = mutableListOf<NutritionFragment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)
        binding = FragmentDetailsBinding.bind(view)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.get("RECENT_ACTIVITY")?.let {
            displayData(it as RecentActivity)
        }
        requireArguments().getString("IMAGE_URI")?.run {
            onAnalyzeImage(uri = Uri.parse(this))
        }
        setupColor()

    }

    private fun initNutritionsViewPager() {
        vpNutritionsPagerAdapter = NutritionsPagerAdapter(this, listOfNutritionFragments)
        binding.vpNutritions.adapter = vpNutritionsPagerAdapter
    }

    private fun setupColor() {
        color = Color.GREEN
        color?.let {
            boxPaint.color = it
            boxPaint.style = Paint.Style.STROKE; // stroke or fill or ...
            boxPaint.strokeWidth = 5.toFloat()
            textPaint.color = it
            textPaint.style = Paint.Style.FILL
            textPaint.textSize = 48.toFloat()
        }
    }

    private fun onAnalyzeImage(uri: Uri?) {
        if (uri == null) return
        val bitmap = getBitmap(uri)
        val scaledImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val encodedBitmap = Utils.encodeBitmap(scaledImage)
        val params = DetectionRequest(encodedImage = encodedBitmap)
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val response = retrofit.detectObject(params)
            launch(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.imgCapture.setImageBitmap(scaledImage)
                canvas = Canvas(scaledImage)
                response.detectedObject.forEach {
                    canvas.drawRect(
                        Rect(
                            it.foodBoundingbox.foodX,
                            it.foodBoundingbox.foodY,
                            it.foodBoundingbox.foodX + it.foodBoundingbox.foodWidth,
                            it.foodBoundingbox.foodY + it.foodBoundingbox.foodHeight
                        ),
                        boxPaint
                    )
                    canvas.drawText(
                        it.foodLabel,
                        it.foodBoundingbox.foodX.toFloat(),
                        it.foodBoundingbox.foodY.toFloat(),
                        textPaint
                    )
                    listOfNutritionFragments.add(
                        NutritionFragment.newInstance(it.foodLabel)
                    )
                }
                initNutritionsViewPager()
                binding.root.invalidate()
                launch(Dispatchers.IO) {
                    uploadToFirebase(response, scaledImage)
                }
            }
        }
    }

    private fun displayData(data: RecentActivity) {
        val image = Utils.decodeBase64toBitmap(data.encodedImage ?: "")
        binding.imgCapture.setImageBitmap(image)
        data.detectedObject?.forEach {
            listOfNutritionFragments.add(
                NutritionFragment.newInstance(it)
            )
        }
        initNutritionsViewPager()
    }

    private fun uploadToFirebase(response: DetectionResponse, bitmap: Bitmap) {
        val resizedBitmap = getResizedBitmap(bitmap, 1024, 1024)
        val encodedBitmap = Utils.encodeBitmap(resizedBitmap!!)
        repo.addRecentActivity(
            RecentActivity(
                encodedImage = encodedBitmap,
                detectedObject = response.detectedObject.map {
                    it.foodLabel
                }
            ),
            onSuccess = {
            },
            onError = {
            }
        )
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
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    requireContext().contentResolver,
                    uri
                )
            )
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )

        return resizedBitmap
    }
}