package com.example.musicplayer.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.R
import com.example.musicplayer.song_ui.SongUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

object MyPlayer {

    enum class PlayType {
        NEXT, PREV, CURRENT
    }

    private var bluetoothCommand: Boolean = true
    lateinit var player: ExoPlayer
    private var focus: AudioFocus? = null
    private var activeSongUI: WeakReference<SongUI>? = null
    var songs: MutableList<MediaItem> = mutableListOf()

    val music: MediaItem?
        get() = player.currentMediaItem

    var onSongPrepared: (() -> Unit) = {}

    val isPlaying: Boolean
        get() = player.isPlaying

    val currentPosition: Int
        get() = player.currentPosition.toInt()

    val duration: Int
        get() = player.duration.toInt()

    fun setVolume(x: Float) {
        if (x in 0.0..1.0)
            player.volume = x
    }

    fun initPlayer(context: Context) {
        player = ExoPlayer.Builder(context).build()
        player.addListener(createListener())
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

    fun startSong(context: Context, playType: PlayType) {
        bluetoothCommand = false
        focus?.abandonFocus()
        focus = AudioFocus(context)
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
//            CoroutineScope(Dispatchers.IO).launch {
//                Thread.sleep(200)
//            }
            if (isPlaying) {
                refreshUI()
                Log.d(TAG, "IS PLAYING TRUE!!!")
            }
            else if (!player.playWhenReady || bluetoothCommand) {
                refreshUI()
                Log.d(TAG, "IS PLAYING FALSE!!!")
                bluetoothCommand = true
            }
            else {
                Log.d(TAG, "IS PLAYING FALSE BUT WANTS NEXT")
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

    fun forcePlaySong() {
        player.play()
//        refreshUI()
    }

    fun forcePauseSong() {
        player.pause()
//        refreshUI()
    }

    fun pauseSong() {
        forcePauseSong()
        focus?.abandonFocus()
    }

    fun playSong() {
        focus?.let {
            if (it.requestFocus()) {
                forcePlaySong()
            }
        }
    }

    fun songMove(duration: Int) {
        player.seekTo(duration.toLong())
    }

    fun destroy() {
        player.release()
        focus?.abandonFocus()
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