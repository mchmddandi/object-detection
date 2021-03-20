package com.example.objectdetection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.objectdetection.databinding.PickImageItemBinding

class PickImageOptionAdapter :
        RecyclerView.Adapter<PickImageOptionAdapter.PickImageOptionViewHolder>() {
    private val items = mutableListOf<PickImage>()

    class PickImageOptionViewHolder(private val binding: PickImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String, onClick: () -> Unit) {
            binding.tvPickImageOption.text = text
            binding.tvPickImageOption.setOnClickListener {
                onClick.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickImageOptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pick_image_item, parent, false)
        return PickImageOptionViewHolder(
                PickImageItemBinding.bind(view)
        )
    }

    override fun onBindViewHolder(holder: PickImageOptionViewHolder, position: Int) {
        holder.bind(items[position].text, items[position].onClick)
    }

    override fun getItemCount() = items.size

    fun setItems(items: List<PickImage>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    data class PickImage(
            val text: String,
            val onClick: () -> Unit
    )
}