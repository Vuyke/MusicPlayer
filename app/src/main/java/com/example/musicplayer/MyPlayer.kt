package com.example.musicplayer

import android.content.ContentValues.TAG
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.lang.ref.WeakReference
import java.util.ArrayList

object MyPlayer {

    private lateinit var player: MediaPlayer
    private var focus: AudioFocus? = null
    private val queue = ArrayList<MusicItem>()
    private var currentIndex = 0
    private var activeSongUI: WeakReference<SongUI>? = null
    var music: MusicItem? = null

    val isPlaying: Boolean
        get() = player.isPlaying

    val currentPosition: Int
        get() = player.currentPosition

    val duration: Int
        get() = player.duration

    fun setVolume(x: Float, y: Float) {
        if (x >= 0 && x <= 1 && y >= 0 && y <= 1)
            player.setVolume(x, y)
    }

    fun initPlayer() {
        player = MediaPlayer()
    }

    fun setActiveSongUI(layout: SongUI) {
        activeSongUI = WeakReference(layout)
    }

    fun initQueue(songs: MutableList<MusicItem>, ind: Int) {
        queue.clear()
        for (i in 0 until songs.size) {
            queue.add(songs[i])
        }
        currentIndex = ind
    }

    fun nextSong(): MusicItem? {
        if (currentIndex + 1 < queue.size)
            return queue[++currentIndex]
        return null
    }

    fun previousSong(): MusicItem? {
        if (currentPosition > 4000) {
            return queue[currentIndex]
        }
        if (currentIndex > 0) {
            return queue[--currentIndex]
        }
        return null
    }


    fun startSong(context: Context, musicItem: MusicItem?, startPlaying: Boolean = true) {
        focus?.abandonFocus()
        if (musicItem == null) {
            startSong(context, queue[queue.size - 1], false)
            return
        }
        music = musicItem
        focus = AudioFocus(context)
        val currentBottom = activeSongUI?.get()
        try {
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
            player.setDataSource(musicItem.path)
            player.setOnPreparedListener(null)
            player.setOnCompletionListener(null)
            player.setOnPreparedListener {
                if (startPlaying) {
                    playSong()
                }
                currentBottom?.refresh()
            }
            player.setOnCompletionListener {
                player.stop()
                focus?.abandonFocus()
                startSong(context, nextSong())
            }
            player.prepareAsync()
        }
        catch(e:Exception) {
            Log.d(TAG, "Error playing the song!")
        }
    }

    fun pauseSong() {
        player.pause()
        focus?.abandonFocus()
    }

    fun playSong() {
        focus?.let {
            if (it.requestFocus()) {
                player.start()
            }
        }
    }

    fun forcePlaySong() {
        player.start()
        activeSongUI?.get()?.refresh()
    }

    fun forcePauseSong() {
        player.pause()
        activeSongUI?.get()?.refresh()
    }

    fun moveSong(duration: Int) {
        player.seekTo(duration)
    }

    fun destroy() {
        player.release()
        focus?.abandonFocus()
    }
}