package com.imagetools.select.adapter2

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.imagetools.select.R
import com.imagetools.select.adapter2.base.BaseAdapter2
import com.imagetools.select.adapter2.base.BaseSelectAdapter2
import com.imagetools.select.entity.Image

/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
internal class ImageSelectAdapter2(
    context: Context,
    isMultiple: Boolean = false,
    selectLimit: Int = 9,
    columnCount: Int = 4
) :
    BaseSelectAdapter2<Image>(context, arrayListOf(), isMultiple, selectLimit, columnCount) {

    override fun getView(position: Int, converView: View?, parent: ViewGroup?): View {
        val itemView: View
        val viewHolder: ViewHolder
        if (converView == null) {
            itemView = layoutInflater.inflate(R.layout.item_image, parent, false)
            viewHolder = ViewHolder(
                itemView.findViewById(R.id.iv_image),
                itemView.findViewById(R.id.iv_mask),
                itemView.findViewById(R.id.frame_select),
                itemView.findViewById(R.id.tv_select_number)
            )
            itemView.tag = viewHolder
        } else {
            itemView = converView
            viewHolder = itemView.tag as ViewHolder
        }

        val item = getItem(position)
        item.itemIndex = position
        viewHolder.frameSelect.visibility = View.GONE

        viewHolder.ivImage.layoutParams = ConstraintLayout.LayoutParams(size, size)
        Glide.with(context).load(item.path)
            .placeholder(ColorDrawable(ContextCompat.getColor(context, R.color.colorSelect)))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    viewHolder.ivImage.setImageDrawable(resource)
                    if (isMultiple) {
                        viewHolder.frameSelect.visibility = View.VISIBLE
                    }
                    return true
                }

            })
            .into(viewHolder.ivImage)

        if (isMultiple) {
            if (item.select) {
                viewHolder.ivMask.visibility = View.VISIBLE
                viewHolder.tvSelectNumber.text = selectList.indexOf(item).plus(1).toString()
                viewHolder.tvSelectNumber.setBackgroundResource(R.drawable.shape_select_number_press)
            } else {
                viewHolder.ivMask.visibility = View.GONE
                viewHolder.tvSelectNumber.text = ""
                viewHolder.tvSelectNumber.setBackgroundResource(R.drawable.shape_select_number_normal)
            }

            viewHolder.frameSelect.setOnClickListener {
                updateSelected(getItem(position))
            }
        }

        return itemView
    }

    class ViewHolder(
        val ivImage: ImageView,
        val ivMask: ImageView,
        val frameSelect: FrameLayout,
        val tvSelectNumber: TextView
    )

    fun updateSelected(item: Image) {
        if (!item.select && selectList.size == selectLimit) {
            mSelectCallback?.selectEnd(selectLimit)
            return
        }

        if (item.select) {
            item.select = false
            selectList.remove(item)
            notifyDataSetChanged()
        } else {
            item.select = true
            selectList.add(item)
        }

        notifyDataSetChanged()
        mSelectCallback?.selectCall(selectList.size)
    }

}