package com.example.objectdetection.details.nutrition

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.objectdetection.R
import com.example.objectdetection.databinding.NutritionItemBinding

class NutritionAdapter : RecyclerView.Adapter<NutritionAdapter.NutritionViewHolder>() {
    private val items = mutableListOf<Nutrition>()

    class NutritionViewHolder(private val binding: NutritionItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Nutrition){
            binding.tvNutritionLabel.text = item.label
            binding.tvNutritionValue.text = item.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutritionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nutrition_item,parent,false)
        val binding = NutritionItemBinding.bind(view)
        return NutritionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NutritionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun setItems(items: List<Nutrition>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }



    data class Nutrition(
        val label: String,
        val value: String
    )
}