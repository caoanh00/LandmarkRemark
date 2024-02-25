package com.example.landmarkremark.ui.collections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.landmarkremark.databinding.ViewHolderCollectionListBinding
import com.example.landmarkremark.interfaces.IRecyclerViewListener
import com.example.landmarkremark.models.LocationData
import java.text.SimpleDateFormat
import java.util.Date

class CollectionsAdapter(val onRecyclerViewOnClickListener: IRecyclerViewListener) :
    RecyclerView.Adapter<CollectionsAdapter.CollectionViewHolder>() {

    private var collectionList: MutableList<LocationData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val binding = ViewHolderCollectionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return collectionList.size
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.onBind(position, collectionList, onRecyclerViewOnClickListener)

    }

    fun setList(notificationList: MutableList<LocationData>?) {
        this.collectionList = notificationList ?: mutableListOf()
        notifyDataSetChanged()
    }

    class CollectionViewHolder(val binding: ViewHolderCollectionListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            position: Int,
            collectionList: List<LocationData>,
            listener: IRecyclerViewListener
        ) {
            val locationData = collectionList[position]
            binding.root.setOnClickListener {
                listener.onRecyclerViewItemClickListener(locationData)
            }

            binding.collectionTitle.text = locationData.title
            binding.collectionDescription.text = locationData.description

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            locationData.createdTime?.let {
                val nowTime = dateFormat.format(Date(locationData.createdTime.toLong()))
                binding.collectionCreatedTime.text = nowTime
            }
        }
    }
}