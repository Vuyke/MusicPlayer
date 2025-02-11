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
    val artist: TextView = view.findViewById(R.id.artist)
    private val song: TextView = view.findViewById(R.id.song)
    private val playButton: ImageView = view.findViewById(R.id.play_pause_button)
    val albumArt: ImageView = view.findViewById(R.id.albumArt)
    protected val context: Context = view.context
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressBar = object : Runnable {
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
            artist.text = it.artist
            song.text = it.name
            Util.getImageView(it.artPath, context, albumArt)
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