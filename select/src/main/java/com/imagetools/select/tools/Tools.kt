package com.imagetools.select.tools

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import com.imagetools.select.R
import java.io.ByteArrayOutputStream


/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal object Tools {

    fun viewTranslationHide(view: View) {
        view.translationY = (-view.height).toFloat()
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    fun getStatusBarHeight(context: Context): Int {
        val resId =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) {
            context.resources.getDimensionPixelSize(resId)
        } else 0
    }

    fun selectViewTranslationAnimator(
        enable: Boolean,
        containerView: View,
        maskView: View
    ): ValueAnimator {
        val dimen = containerView.height.toFloat()
        val animator =
            if (enable) ValueAnimator.ofFloat(-dimen, 0F)
            else ValueAnimator.ofFloat(0F, -dimen)
        animator.duration = 200
        animator.addUpdateListener {
            maskView.alpha = (dimen - Math.abs(it.animatedValue as Float)) / dimen
            containerView.alpha = (dimen - Math.abs(it.animatedValue as Float)) / dimen
            containerView.translationY = it.animatedValue as Float
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if (enable) return
                maskView.visibility = View.GONE
                containerView.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationStart(animation: Animator) {
                if (!enable) return
                maskView.visibility = View.VISIBLE
                containerView.visibility = View.VISIBLE
            }

        })
        animator.start()
        return animator
    }

    fun getItemOrderAnimator(context: Context): LayoutAnimationController {
        val animController =
            LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.item_alpha_in))
        animController.order = LayoutAnimationController.ORDER_NORMAL
        animController.delay = 0.1f
        return animController
    }

    fun getImageSize(context: Context, columnCount: Int = 4): Int {
        val windowManager =
            context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val padding = context.resources.getDimension(R.dimen.item_padding)
        val width = point.x.toFloat() - (padding * columnCount.plus(1))
        return (width / columnCount).toInt()
    }

    /**
     * dp转px
     *
     * @param context 上下文
     * @param dpValue dp值
     * @return px值
     */
    fun dp2px(context: Context, dpValue: Int): Float {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f)
    }

    /**
     * px转dp
     *
     * @param context 上下文
     * @param pxValue px值
     * @return dp值
     */
    fun px2dp(context: Context, pxValue: Int): Float {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f)
    }

    /**
     * sp转px
     *
     * @param context 上下文
     * @param spValue sp值
     * @return px值
     */
    fun sp2px(context: Context, spValue: Int): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f)
    }


    /**
     * bitmap转化成byte数组
     * @param bm 需要转换的Bitmap
     * @return
     */
    fun bitmap2Bytes(bm: Bitmap): ByteArray {
        val baoStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baoStream)
        return baoStream.toByteArray()
    }
}