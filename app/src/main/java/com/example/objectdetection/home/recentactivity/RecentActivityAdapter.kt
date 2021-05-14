package com.example.objectdetection.home.recentactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.objectdetection.R
import com.example.objectdetection.Utils
import com.example.objectdetection.databinding.RecentActivityItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentActivityAdapter :
    RecyclerView.Adapter<RecentActivityAdapter.RecentActivityViewHolder>() {
    private val items = mutableListOf<RecentActivity>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var onItemClick: ((RecentActivity) -> Unit)? = null

    class RecentActivityViewHolder(
        private val binding: RecentActivityItemBinding,
        private val coroutineScope: CoroutineScope,
        private val onItemClick: ((RecentActivity) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecentActivity) {
            coroutineScope.launch {
                val decodedImage = Utils.decodeBase64toBitmap(item.encodedImage ?: "")
                withContext(Dispatchers.Main) {
                    binding.ivRecentActivity.setImageBitmap(decodedImage)
                }
            }
            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_activity_item, parent, false)
        val binding = RecentActivityItemBinding.bind(view)
        return RecentActivityViewHolder(binding, coroutineScope, onItemClick)
    }

    override fun onBindViewHolder(holder: RecentActivityViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItem(data: List<RecentActivity>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun setOnItemClick(onItemClick: (RecentActivity) -> Unit) {
        this.onItemClick = onItemClick
    }
}