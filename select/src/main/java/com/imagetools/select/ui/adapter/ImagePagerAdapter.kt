package com.imagetools.select.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.imagetools.select.R
import com.imagetools.select.entity.Image
import com.imagetools.select.ui.widget.DragImageView

/**
 * @author jv.lee
 * @date 2020/12/16
 * @description
 */
internal class ImagePagerAdapter(val data: MutableList<Image>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImagePagerViewHolder>() {

    private var mDragCallback: DragImageView.Callback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePagerViewHolder {
        return ImagePagerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_page_image_imagetools, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ImagePagerViewHolder, position: Int) {
        holder.bindView(data[position], position)
    }

    internal inner class ImagePagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val view by lazy { itemView.findViewById<DragImageView>(R.id.drag_image) }
        fun bindView(item: Image, position: Int) {
            view.setScaleValue(1f)
            view.setMaxScale(3f)
            Glide.with(view)
                .load(item.path)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(view)
            mDragCallback?.let {
                view.setCallback(it)
            }
        }
    }

    fun setBackgroundAlphaCompat(view: View?, alpha: Int) {
        view ?: return
        val mutate = view.background.mutate()
        if (mutate != null) {
            mutate.alpha = alpha
        } else {
            view.background.alpha = alpha
        }
    }

    fun setDragCallback(dragCallback: DragImageView.Callback) {
        this.mDragCallback = dragCallback
    }
}