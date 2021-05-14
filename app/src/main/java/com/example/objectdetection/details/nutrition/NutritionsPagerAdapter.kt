package com.example.objectdetection.details.nutrition

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class NutritionsPagerAdapter(private val fragment: Fragment, private val listOfNutritionScreens: List<NutritionFragment>): FragmentStateAdapter(fragment) {
    override fun getItemCount() = listOfNutritionScreens.size

    override fun createFragment(position: Int): Fragment {
        return listOfNutritionScreens[position]
    }
}