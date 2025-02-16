package com.example.musicplayer.song_ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.R
import com.example.musicplayer.utils.Util

abstract class SongUI(view: View) {
    abstract val format: Int
    val artist: TextView = view.findViewById(R.id.artist)
    private val song: TextView = view.findViewById(R.id.song)
    private val playButton: ImageView = view.findViewById(R.id.play_pause_button)
    val albumArt: ImageView = view.findViewById(R.id.albumArt)
    protected val context: Context = view.context
    protected val handler = Handler(Looper.getMainLooper())
    protected val updateProgressBar = object : Runnable {
        override fun run() {
            updateProgress(MyPlayer.currentPosition)
            handler.postDelayed(this, 750)
        }
    }

    init {
        playButton.setOnClickListener {
            MyPlayer.changeState()
            playButton.setImageResource(MyPlayer.playPauseResource())
        }
    }

    open fun refresh() {
        MyPlayer.music?.let {
            artist.text = it.mediaMetadata.artist
            song.text = it.mediaMetadata.title
            Util.getImageView(it.mediaMetadata.artworkUri, context, albumArt, format)
            playButton.setImageResource(MyPlayer.playPauseResource())
            if (MyPlayer.isPlaying)
                handler.post(updateProgressBar)
            else
                handler.removeCallbacks(updateProgressBar)
            updateDuration()
        }
    }

    abstract fun updateDuration()

    abstract fun updateProgress(current: Int)
}