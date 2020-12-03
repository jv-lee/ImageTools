package com.lee.imagetools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lee.imagetools.entity.Album
import com.lee.imagetools.entity.Image
import com.lee.imagetools.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal class ImageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository by lazy {
        ImageRepository(
            application
        )
    }

    private var tempId: Long = 0L

    val albumsLiveData by lazy { MutableLiveData<List<Album>>() }
    val imagesLiveData by lazy { MutableLiveData<List<Image>>() }

    fun getAlbums() {
        viewModelScope.launch {
            albumsLiveData.value = withContext(Dispatchers.IO) { repository.getAlbums() }
        }
    }

    fun getImagesByAlbumId(id: Long) {
        if (tempId == id) return
        tempId = id
        viewModelScope.launch {
            imagesLiveData.value = withContext(Dispatchers.IO) { repository.getImagesByAlbum(id) }
        }
    }

}