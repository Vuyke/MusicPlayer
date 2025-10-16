package com.example.musicplayer.viewModel

import android.app.Application
import android.content.ComponentName
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayer.activities.MusicPlayerService
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.utils.Util
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongViewModel(app: Application) : AbstractPlayerViewModel(app) {
    private var searchJob: Job? = null

    private val _shuffleMode = MutableLiveData<Boolean>()
    val shuffleMode: LiveData<Boolean> get() = _shuffleMode

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

    fun searchUpdated(text: String) {
        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.Main).launch {
            delay(250)
            withContext(Dispatchers.IO) {
                Util.songs.updateSearch(text)
            }
        }
    }

    fun playerChangeShuffleMode() {
        player.changeShuffleMode()
        _shuffleMode.postValue(player.currentShuffleMode)
    }
}