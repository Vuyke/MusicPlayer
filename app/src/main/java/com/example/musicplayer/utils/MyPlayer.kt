package com.example.musicplayer.utils

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.musicplayer.data_class.PlayType


class MyPlayer (private val player: Player) {
    val isPlaying: Boolean
        get() = player.isPlaying

    val currentPosition: Long
        get() = player.currentPosition

    val duration: Long
        get() = player.duration

    val currentSong: MediaItem?
        get() = player.currentMediaItem

    val currentShuffleMode: Boolean
        get() = player.shuffleModeEnabled

    fun update(mediaItem: MediaItem, index: Int) {
        val curPosition = currentPosition
        player.replaceMediaItem(index, mediaItem)
        if (player.currentMediaItemIndex == index) {
            player.seekTo(curPosition)
        }
    }

    fun delete(index: Int) {
        player.removeMediaItem(index)
    }

    private fun initQueue(ind: Int) {
        player.seekTo(Util.songs.getIndex(ind), 0)
    }

    fun startSongFromPosition(ind: Int) {
        initQueue(ind)
        startSong(PlayType.CURRENT)
        Util.logMediaItemInfo(ind)
    }

    fun startSong(playType: PlayType) {
        try {
            if(playType == PlayType.NEXT)
                player.seekToNextMediaItem()
            else if(playType == PlayType.PREV)
                player.seekToPreviousMediaItem()
            playSong()
        }
        catch(e: Exception) {
            Log.d("Application error", "Error playing the song!")
        }
    }

    fun pauseSong() {
        player.pause()
    }

    fun playSong() {
        player.play()
    }

    fun songMove(duration: Long) {
        player.seekTo(duration)
    }

    fun changeShuffleMode() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun destroy() {
        player.release()
    }

    fun changeState() {
        if (isPlaying)
            pauseSong()
        else
            playSong()
    }

    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }
}