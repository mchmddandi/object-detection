package com.example.objectdetection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.objectdetection.databinding.PickImageMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PickImageOptionBottomSheet(
        private val imageOptions: List<PickImageOptionAdapter.PickImage>
) : BottomSheetDialogFragment() {

    private val pickImageOptionAdapter by lazy {
        PickImageOptionAdapter()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pick_image_menu, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = PickImageMenuBinding.bind(view)
        binding.listPickImageMenu.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pickImageOptionAdapter
        }
        pickImageOptionAdapter.setItems(imageOptions)
    }

    companion object {
        const val TAG = "PickImageOptionBottomSheet"
        fun newInstance(imageOptions: List<PickImageOptionAdapter.PickImage>): PickImageOptionBottomSheet {
            return PickImageOptionBottomSheet(imageOptions)
        }
    }


}