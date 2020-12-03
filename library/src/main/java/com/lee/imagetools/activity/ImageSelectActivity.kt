package com.lee.imagetools.activity

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.lee.imagetools.R
import com.lee.imagetools.adapter.AlbumSelectAdapter
import com.lee.imagetools.adapter.ImageMultipleSelectAdapter
import com.lee.imagetools.adapter.ImageSingleSelectAdapter
import com.lee.imagetools.adapter.SelectAdapter
import com.lee.imagetools.constant.Constants
import com.lee.imagetools.entity.Album
import com.lee.imagetools.entity.Image
import com.lee.imagetools.entity.SelectConfig
import com.lee.imagetools.intent.CropActivityResult
import com.lee.imagetools.tools.Tools
import com.lee.imagetools.viewmodel.ImageViewModel
import com.lee.imagetools.widget.ImageSelectBar

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal class ImageSelectActivity : BaseActivity(R.layout.activity_image_select) {

    private val viewModel by viewModels<ImageViewModel>()
    private val selectConfig by lazy { intent.getParcelableExtra<SelectConfig>(Constants.CONFIG_KEY) }
    private var animator: ValueAnimator? = null

    private val imageSelectBar by lazy { findViewById<ImageSelectBar>(R.id.image_select_bar) }
    private val viewMask by lazy { findViewById<View>(R.id.mask) }
    private val rvSelect by lazy { findViewById<RecyclerView>(R.id.rv_select) }
    private val rvImages by lazy { findViewById<RecyclerView>(R.id.rv_images) }
    private val tvReview by lazy { findViewById<TextView>(R.id.tv_review) }
    private val tvDone by lazy { findViewById<TextView>(R.id.tv_done) }
    private val constNavigation by lazy { findViewById<ConstraintLayout>(R.id.const_navigation) }

    private val mSelectAdapter by lazy {
        AlbumSelectAdapter().also {
            it.setOnItemClickListener(object : SelectAdapter.ItemClickListener<Album> {
                override fun onClickItem(position: Int, item: Album) {
                    imageSelectBar.setSelectName(item.name)
                    imageSelectBar.switch()
                    viewModel.getImagesByAlbumId(item.id)
                }
            })
        }
    }

    private val mImagesAdapter by lazy {
        if (selectConfig.isMultiple)
            ImageMultipleSelectAdapter(selectConfig.selectCount).also {
                it.setOnItemClickListener(object : SelectAdapter.ItemClickListener<Image> {
                    override fun onClickItem(position: Int, item: Image) {
                        it.updateSelected(item)
                    }

                })
                it.setSelectCallback(object : ImageMultipleSelectAdapter.SelectCallback {
                    override fun selectEnd(limit: Int) {
                        Toast.makeText(
                            this@ImageSelectActivity,
                            getString(R.string.select_limit_description, limit),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun selectCall(count: Int) {
                        this@ImageSelectActivity.selectDoneCount(count)
                    }

                })
            }
        else
            ImageSingleSelectAdapter().also {
                it.setOnItemClickListener(object : SelectAdapter.ItemClickListener<Image> {
                    override fun onClickItem(position: Int, item: Image) {
                        //裁剪请求
                        imageLaunch.launch(item)
                    }
                })
            }
    }

    /**
     * 单图裁剪后返回
     */
    private val imageLaunch by lazy {
        registerForActivityResult(CropActivityResult(selectConfig.isSquare)) {
            it ?: return@registerForActivityResult
            setResult(
                Constants.IMAGE_DATA_RESULT_CODE,
                Intent().putParcelableArrayListExtra(Constants.IMAGE_DATA_KEY, arrayListOf(it))
            )
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLaunch
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            throw RuntimeException("Please apply for 'Manifest.permission.WRITE_EXTERNAL_STORAGE' permission first")
        }
        bindView()
        bindListener()
        bindObservable()
    }

    private fun bindView() {
        constNavigation.visibility = if (selectConfig.isMultiple) View.VISIBLE else View.GONE

        rvSelect.layoutManager = LinearLayoutManager(this)
        rvSelect.adapter = mSelectAdapter

        (rvImages.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvImages.layoutManager = GridLayoutManager(this, 4)
        rvImages.adapter = mImagesAdapter
    }

    private fun bindListener() {
        tvDone.setOnClickListener {
            setResult(
                Constants.IMAGE_DATA_RESULT_CODE,
                Intent().putExtra(
                    Constants.IMAGE_DATA_KEY,
                    (mImagesAdapter as ImageMultipleSelectAdapter).getSelectList()
                )
            )
            finish()
        }
        viewMask.setOnClickListener {
            if (imageSelectBar.getEnable()) {
                imageSelectBar.switch()
            }
        }
        rvSelect.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val view = rv.findChildViewUnder(e.x, e.y)
                if (view == null && imageSelectBar.getEnable()) {
                    imageSelectBar.switch()
                }
                return super.onInterceptTouchEvent(rv, e)
            }
        })
        imageSelectBar.setAnimCallback(object : ImageSelectBar.AnimCallback {
            override fun animEnd() {
                mImagesAdapter.notifyDataSetChanged()
            }

            override fun animCall(enable: Boolean) {
                animator = Tools.selectViewTranslationAnimator(enable, rvSelect, viewMask)
            }
        })
    }

    private fun bindObservable() {
        viewModel.albumsLiveData.observe(this, Observer {
            mSelectAdapter.updateData(it)
            Tools.viewTranslationHide(rvSelect)
        })

        viewModel.imagesLiveData.observe(this, Observer {
            if (mImagesAdapter is ImageMultipleSelectAdapter) {
                (mImagesAdapter as ImageMultipleSelectAdapter).getSelectList().clear()
                selectDoneCount(0)
            }
            rvImages.layoutAnimation = Tools.getItemOrderAnimator(this)
            if (mImagesAdapter.getData().isEmpty()) {
                mImagesAdapter.updateData(it)
            } else {
                mImagesAdapter.getData().clear()
                mImagesAdapter.getData().addAll(it)
            }

        })

        viewModel.getAlbums()
        viewModel.getImagesByAlbumId(Constants.DEFAULT_ALBUM_ID)
    }

    override fun onPause() {
        super.onPause()
        Tools.bindBottomFinishing(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        animator?.cancel()
        animator = null
    }

    private fun selectDoneCount(count: Int) {
        if (count == 0) {
            tvDone.setText(R.string.done_text)
        } else {
            tvDone.text = getString(R.string.done_format_text, count)
        }
        checkNavigationView(count > 0)
    }

    private fun checkNavigationView(enable: Boolean) {
        val textColor = ContextCompat.getColor(
            this,
            if (enable) R.color.colorText else R.color.colorTextPair
        )
        val textBackground = ContextCompat.getDrawable(
            this,
            if (enable) R.drawable.shape_button_press else R.drawable.shape_button_normal
        )
        tvReview.setTextColor(textColor)
        tvReview.isClickable = enable
        tvDone.setTextColor(textColor)
        tvDone.background = textBackground
        tvDone.isClickable = enable
    }

}