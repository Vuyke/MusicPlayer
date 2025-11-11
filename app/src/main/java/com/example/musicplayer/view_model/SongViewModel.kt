package com.example.musicplayer.view_model

import android.app.Application
import android.content.ComponentName
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayer.activities.MusicPlayerService
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.utils.Util
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel(app: Application) : AbstractPlayerViewModel(app) {
    private var searchJob: Job? = null

    private val _shuffleMode = MutableLiveData<Boolean>()
    val shuffleMode: LiveData<Boolean> get() = _shuffleMode

    private val _sortOccurred = MutableLiveData(false)
    val sortOccurred: LiveData<Boolean> get() = _sortOccurred

    init {
        val sessionToken = SessionToken(getApplication(), ComponentName(getApplication(), MusicPlayerService::class.java))
        val future =  MediaController.Builder(getApplication(), sessionToken).buildAsync()
        future.addListener({listenerForMediaController(future)}, ContextCompat.getMainExecutor(getApplication()))
    }

    private fun listenerForMediaController(future: ListenableFuture<MediaController>) {
        try {
            val controller = future.get()
            val tempPlayer = MyPlayer(controller)
            player = tempPlayer
            player.addListener(createListener())
            onPlayerSetupInitializeFields()

        } catch (e: Exception) {
            Log.e("File", "Error getting MediaController", e)
        }
    }

    private fun onPlayerSetupInitializeFields() {
        _song.postValue(player.currentSong ?: MediaItem.EMPTY)
        _shuffleMode.postValue(player.currentShuffleMode)
        _progress.postValue(player.currentPosition)
        updateDuration(player.duration)
        updateIsPlaying(player.isPlaying)
    }

    private fun updateSortOccurred() {
        _sortOccurred.postValue(!sortOccurred.value!!)
    }

    fun searchUpdated(text: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            Util.songs.updateSearch(text)
            updateSortOccurred()
        }
    }

    fun playerChangeShuffleMode() {
        player.changeShuffleMode()
        _shuffleMode.postValue(player.currentShuffleMode)
    }

    fun sortArtist() {
        Util.songs.sortArtist()
        updateSortOccurred()
    }

    fun sortDefault() {
        Util.songs.sortDefault()
        updateSortOccurred()
    }
}