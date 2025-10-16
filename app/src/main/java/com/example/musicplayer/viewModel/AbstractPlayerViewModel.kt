package com.example.musicplayer.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.musicplayer.data_class.PlayType
import com.example.musicplayer.utils.MyPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class AbstractPlayerViewModel(app: Application): AndroidViewModel(app) {
    protected lateinit var player: MyPlayer
    private var progressJob: Job? = null

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    protected val _song = MutableLiveData<MediaItem>()
    val song: LiveData<MediaItem> get() = _song

    private val _duration = MutableLiveData<Long>()
    val duration: LiveData<Long> get() = _duration

    protected val _progress = MutableLiveData<Long>()
    val progress: LiveData<Long> get() = _progress

    private val _somethingPlayed = MutableLiveData<Boolean>()
    val somethingPlayed: LiveData<Boolean> get() = _somethingPlayed

    protected fun updateDuration(duration: Long) {
        if (duration != C.TIME_UNSET) {
            _duration.postValue(duration)
        }
    }

    protected fun updateIsPlaying(currentlyPlaying: Boolean) {
        _isPlaying.postValue(currentlyPlaying)
        if (currentlyPlaying) {
            _somethingPlayed.postValue(true)
            observeProgress()
        }
        else {
            progressJob?.cancel()
        }
    }

    open fun observeProgressAdditional() {}

    private fun observeProgress() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                _progress.postValue(player.currentPosition)
                observeProgressAdditional()
                delay(250L)
            }
        }
    }

    fun playerSeekSong() {
        playerSeekSong(0)
    }

    fun playerSeekSong(ind: Int) {
        player.startSongFromPosition(ind)
    }

    fun playerUpdateSong(song: MediaItem, ind: Int) {
        player.update(song, ind)
    }

    fun playerDeleteSong(ind: Int) {
        player.delete(ind)
    }

    fun playerPlayNext() {
        player.startSong(PlayType.NEXT)
    }

    fun playerPlayPrevious() {
        player.startSong(PlayType.PREV)
    }

    fun playerMoveSong(progress: Long) {
        player.songMove(progress)
    }

    fun playerPlaySong() {
        player.playSong()
    }

    fun playerPauseSong() {
        player.pauseSong()
    }

    fun playerChangeIsPlaying() {
        player.changeState()
    }

    protected fun createListener() = object : Player.Listener {
        override fun onIsPlayingChanged(currentlyPlaying: Boolean) {
            updateIsPlaying(currentlyPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _song.postValue(mediaItem ?: MediaItem.EMPTY)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(Player.EVENT_PLAYBACK_STATE_CHANGED, Player.EVENT_MEDIA_ITEM_TRANSITION, Player.EVENT_TIMELINE_CHANGED)) {
                updateDuration(player.duration)
            }
        }
    }

    override fun onCleared() {
        player.destroy()
        super.onCleared()
    }
}