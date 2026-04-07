package com.example.wallpapersnooper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load

data class WallpaperItem(
    val thumbUrl: String,
    val fullUrl: String,
    val resolution: String,
    val uploader: String,
    val tags: String,
    val fileSize: Long
)

class WallpaperAdapter(
    private val items: List<WallpaperItem>,
    private val onSelect: (WallpaperItem) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivWallpaper)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.image.load(items[position].thumbUrl)
        holder.image.setOnClickListener { onSelect(items[position]) }
    }

    override fun getItemCount() = items.size
}