package com.example.musicplayer.utils

import android.content.ContentValues.TAG
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import com.example.musicplayer.R
import com.example.musicplayer.data_class.PlayType


class MyPlayer (val player: Player) {

//    val mediaSource =
//        ProgressiveMediaSource.Factory(customDataSourceFactory, customExtractorsFactory)
//            .setLoadErrorHandlingPolicy(customLoadErrorHandlingPolicy)
//            .createMediaSource(MediaItem.fromUri(streamUri))

    val music: MediaItem?
        get() = player.currentMediaItem

    val isPlaying: Boolean
        get() = player.isPlaying

    val currentPosition: Int
        get() = player.currentPosition.toInt()

    val duration: Int
        get() = player.duration.toInt()


    fun initQueue(ind: Int) {
        player.seekTo(Util.songs.getIndex(ind), 0)
    }

    fun startSong(playType: PlayType) {
        Util.bluetoothCommand = false
        try {
            if(playType == PlayType.NEXT)
                player.seekToNextMediaItem()
            else if(playType == PlayType.PREV)
                player.seekToPreviousMediaItem()
            playSong()
        }
        catch(e: Exception) {
            Log.d(TAG, "Error playing the song!")
        }
    }

    fun pauseSong() {
        player.pause()
    }

    fun playSong() {
        player.play()
    }

    fun songMove(duration: Int) {
        player.seekTo(duration.toLong())
    }

    fun playPauseResource(): Int {
        return if (player.isPlaying) R.drawable.pause
        else R.drawable.play
    }

    fun destroy() {
        player.release()
    }
}