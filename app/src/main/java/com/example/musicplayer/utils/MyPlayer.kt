package com.example.musicplayer.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.session.SessionToken
import com.example.musicplayer.data_class.MusicItem
import com.example.musicplayer.R
import com.example.musicplayer.song_ui.SongUI
import java.lang.ref.WeakReference

object MyPlayer {

    enum class PlayType {
        NEXT, PREV, CURRENT
    }

    lateinit var player: ExoPlayer
    private var focus: AudioFocus? = null
    private var activeSongUI: WeakReference<SongUI>? = null
    var songs: MutableList<MusicItem> = mutableListOf()

    val music: MusicItem?
        get() = player.currentMediaItem?.let { MusicItem(it) }

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
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSource =

        player.addListener(createListener(context))
        songs = Util.mp3Files(context)
        for (i in 0 until songs.size) {
            player.addMediaItem(songs[i].item)
        }
        player.prepare()
    }

    fun setActiveSongUI(layout: SongUI) {
        activeSongUI = WeakReference(layout)
    }

    fun initQueue(ind: Int) {
        player.seekTo(ind, 0)
    }

    fun startSong(context: Context, playType: PlayType) {
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

    private fun createListener(context: Context) = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                refreshUI()
            }
            else if (!player.playWhenReady) {
                refreshUI()
            }
            Log.d(TAG, "ISPLAYING CHANGED!!!")
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            refreshUI()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if(playbackState == Player.STATE_READY) {

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

    private fun pauseSong() {
        forcePauseSong()
        focus?.abandonFocus()
    }

    private fun playSong() {
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