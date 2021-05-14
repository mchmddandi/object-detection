package com.example.objectdetection.home

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.objectdetection.*
import com.example.objectdetection.databinding.FragmentHomeBinding
import com.example.objectdetection.home.recentactivity.RecentActivityAdapter
import com.example.objectdetection.home.recentactivity.RecentActivityRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private val navController by lazy {
        findNavController()
    }
    private var outputUri: Uri? = null
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                navigateToDetailsScreen(outputUri)
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            navigateToDetailsScreen(it)
        }

    private val recentActivityAdapter by lazy {
        RecentActivityAdapter()
    }

    private val repo by lazy {
        RecentActivityRepositoryImpl(Firestore.getInstance())
    }

    private var pickImageOptionBottomSheet: PickImageOptionBottomSheet? = null
    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupBottomSheet()
        binding.btnAdd.setOnClickListener {
            pickImageOptionBottomSheet?.show(childFragmentManager, PickImageOptionBottomSheet.TAG)
        }
        initView()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = "Nutrition Detection"
        }
    }

    private fun pickFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun launchNativeCamera() {
        createTempFile()
        takePictureLauncher.launch(outputUri)
    }

    private fun initView() {
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentActivityAdapter
        }
        initData()
        recentActivityAdapter.setOnItemClick {
            val bundle = Bundle()
            bundle.putParcelable("RECENT_ACTIVITY", it)
            navController.navigate(R.id.action_homeFragment_to_detailsFragment, bundle)
        }
    }

    private fun initData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            repo.getRecentActivities() {
                binding.progressBar.visibility = View.GONE
                recentActivityAdapter.setItem(it)
            }
        }
    }

    private fun createTempFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = createContentValuesImage()
            requireContext().contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
                .let {
                    outputUri = it
                }
        } else {
            val tempFile = createTempFileImage()
            outputUri = FileProvider.getUriForFile(
                requireContext(),
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

    private fun setupBottomSheet() {
        val pickImageOptions = listOf(
            PickImageOptionAdapter.PickImage(
                text = "Capture Image",
                onClick = {
                    launchNativeCamera()
                    pickImageOptionBottomSheet?.dismiss()
                }
            ),
            PickImageOptionAdapter.PickImage(
                text = "Pick from gallery",
                onClick = {
                    pickFromGallery()
                    pickImageOptionBottomSheet?.dismiss()
                }
            )
        )
        pickImageOptionBottomSheet = PickImageOptionBottomSheet.newInstance(pickImageOptions)
    }

    private fun navigateToDetailsScreen(uri: Uri?) {
        val args = Bundle()
        args.putString("IMAGE_URI", uri.toString())
        navController.navigate(R.id.action_homeFragment_to_detailsFragment, args)
    }
}