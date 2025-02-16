package com.example.musicplayer.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.R
import com.example.musicplayer.data_class.PlayType
import com.example.musicplayer.song_ui.SongUI
import java.lang.ref.WeakReference


object MyPlayer {

    private var bluetoothCommand: Boolean = true
    lateinit var player: ExoPlayer
    private var activeSongUI: WeakReference<SongUI>? = null
    var songs: MutableList<MediaItem> = mutableListOf()
    private var alreadyPlayed = false

//    val mediaSource =
//        ProgressiveMediaSource.Factory(customDataSourceFactory, customExtractorsFactory)
//            .setLoadErrorHandlingPolicy(customLoadErrorHandlingPolicy)
//            .createMediaSource(MediaItem.fromUri(streamUri))

    val music: MediaItem?
        get() = if (alreadyPlayed) player.currentMediaItem else null

    var onSongPrepared: (() -> Unit) = {}

    val isPlaying: Boolean
        get() = player.isPlaying

    val currentPosition: Int
        get() = player.currentPosition.toInt()

    val duration: Int
        get() = player.duration.toInt()

    fun initPlayer(context: Context) {
        player = ExoPlayer.Builder(context).build()
        player.addListener(createListener())
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        player.setAudioAttributes(audioAttributes, true)
        songs = Util.mp3Files(context)
        player.addMediaItems(songs)
        player.prepare()
    }

    fun setActiveSongUI(layout: SongUI) {
        activeSongUI = WeakReference(layout)
    }

    fun initQueue(ind: Int) {
        player.seekTo(ind, 0)
    }

    fun startSong(playType: PlayType) {
        alreadyPlayed = true
        bluetoothCommand = false
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

    private fun createListener() = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(TAG, "State changed!")
        }
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying || bluetoothCommand || !player.playWhenReady) {
                refreshUI()
                Log.d(TAG, "IS PLAYING TRUE!!!")
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                refreshUI()
                Log.d(TAG, "MEDIA TRANSITION")
            }
        }
    }

    private fun refreshUI() {
        onSongPrepared()
        activeSongUI?.get()?.refresh()
    }

    private fun pauseSong() {
        player.pause()
    }

    private fun playSong() {
        player.play()
    }

    fun songMove(duration: Int) {
        player.seekTo(duration.toLong())
    }

    fun destroy() {
        player.release()
    }

    fun playPauseResource(): Int {
        return if (isPlaying) R.drawable.pause
        else R.drawable.play
    }

    fun changeState() {
        if (isPlaying)
            pauseSong()
        else
            playSong()
    }
}