package com.imagetools.app.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.imagetools.app.R


/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
class StreamerView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mStreamerPatin = Paint(Paint.ANTI_ALIAS_FLAG)

    private var columnCount = 4
    private var rowCount = 0

    private var mWidth: Float = 0f
    private var mHeight: Float = 0f
    private var size: Float = 0f
    private var itemPadding: Float = 0F
    private var streamerValue: Float = 0f

    private var itemLineColor = Color.parseColor("#ffffff")
    private var itemColor = Color.parseColor("#4C4C4C")
    private var streamerColor = Color.parseColor("#323232")

    private var completeFlag = false

    init {
        itemPadding = context.resources.getDimension(R.dimen.item_padding)

        mPaint.style = Paint.Style.FILL
        mPaint.color = itemColor
        mPaint.strokeWidth = itemPadding

        mLinePaint.style = Paint.Style.STROKE
        mLinePaint.color = itemLineColor
        mLinePaint.strokeWidth = itemPadding

        mStreamerPatin.style = Paint.Style.FILL
        mStreamerPatin.color = streamerColor
        mStreamerPatin.strokeWidth = itemPadding
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = width.toFloat()
        mHeight = height.toFloat()
        size = (mWidth - (itemPadding * columnCount.plus(1))) / columnCount
        rowCount = (mHeight / size).toInt()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        drawLine(canvas)
        if (!completeFlag) {
            invalidate()
        }
    }

    private fun drawGrid(canvas: Canvas) {
        //绘制填充
        mPaint.shader = buildGradientStreamer(0f, 0f, mWidth, mHeight)
        canvas.drawRect(0f, 0f, mWidth, mHeight, mPaint)
    }

    private fun drawLine(canvas: Canvas) {
        //绘制水平间距
        for (rowIndex in 1..rowCount) {
            canvas.drawLine(
                0f,
                (itemPadding * rowIndex) + (size * rowIndex),
                mWidth,
                (itemPadding * rowIndex) + (size * rowIndex)
                , mLinePaint
            )
        }

        //绘制垂直间距
        for (columnIndex in 0..columnCount) {
            canvas.drawLine(
                (itemPadding * columnIndex) + (size * columnIndex) + (itemPadding / 2),
                0F,
                (itemPadding * columnIndex) + (size * columnIndex) + (itemPadding / 2),
                mHeight
                , mLinePaint
            )
        }
    }

    private fun drawChildView(canvas: Canvas) {
        for (rowIndex in 0..rowCount) {
            for (columnIndex in 0..columnCount) {
                mPaint.shader = buildGradientStreamer(
                    itemPadding + (columnIndex * itemPadding) + (columnIndex * size),
                    itemPadding + (rowIndex * itemPadding) + (rowIndex * size),
                    size + itemPadding + (columnIndex * itemPadding) + (columnIndex * size),
                    size + itemPadding + (rowIndex * itemPadding) + (rowIndex * size)
                )
                canvas.drawRect(
                    itemPadding + (columnIndex * itemPadding) + (columnIndex * size),
                    itemPadding + (rowIndex * itemPadding) + (rowIndex * size),
                    size + itemPadding + (columnIndex * itemPadding) + (columnIndex * size),
                    size + itemPadding + (rowIndex * itemPadding) + (rowIndex * size),
                    mPaint
                )
            }
        }
    }

    private fun buildGradientStreamer(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): LinearGradient {
        val linearGradient =
            LinearGradient(
                left,
                top,
                -(right / 2),
                top,
                intArrayOf(itemColor, streamerColor, itemColor),
                floatArrayOf(0.1f, 0.3f, 0.6f),
                Shader.TileMode.CLAMP
            )
        val matrix = Matrix()
        streamerValue += (right * 0.025).toInt()
        if (streamerValue >= right * 1.5) {
            streamerValue = left
        }
        matrix.setTranslate(streamerValue, 0f)
        linearGradient.setLocalMatrix(matrix)
        return linearGradient
    }

    fun setColumnCount(count: Int) {
        this.columnCount = count
    }

    fun loadComplete() {
        completeFlag = true
        visibility = GONE
    }

}