package com.imagetools.select.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author jv.lee
 * @date 2020/12/2
 * @description
 */
@Parcelize
data class SelectConfig(
    val isMultiple: Boolean = false,
    val selectLimit: Int = 9,
    val isCompress: Boolean = false, //是否使用自带压缩
    val isSquare: Boolean = false,
    val columnCount: Int = 4
) : Parcelable

@Parcelize
data class TakeConfig(
    val isCrop: Boolean = false,
    var isSquare: Boolean = false,
    val isCompress: Boolean = false
) : Parcelable