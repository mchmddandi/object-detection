package com.example.objectdetection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.objectdetection.databinding.FragmentNutritionBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [NutritionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NutritionFragment : Fragment() {
    private var param1: String? = null
    private val nutritionAdapter by lazy {
        NutritionAdapter()
    }
    private lateinit var binding: FragmentNutritionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nutrition, container, false)
        binding = FragmentNutritionBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvNutritions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nutritionAdapter
        }
        binding.tvDetectedObjectLabelNutrition.text = param1
        val data = NutritionDataSource.getNutrition(param1 ?: "")
        nutritionAdapter.setItems(data)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            NutritionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}