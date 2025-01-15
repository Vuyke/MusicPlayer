package com.example.musicplayer

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView

open class SongUI(view: View) {
    val artist: TextView = view.findViewById(R.id.artist)
    val song: TextView = view.findViewById(R.id.song)
    private val playButton: ImageView = view.findViewById(R.id.play_pause_button)
    val albumArt: ImageView = view.findViewById(R.id.albumArt)
    protected val context: Context = view.context

    init {
        playButton.setOnClickListener {
            if(MyPlayer.isPlaying) {
                MyPlayer.pauseSong()
                pause()
            }
            else {
                MyPlayer.playSong()
                play()
            }
        }
    }

    open fun refresh() {
        MyPlayer.music?.let {
            artist.text = it.artist
            song.text = it.name
            Util.getImageView(it.art, context, albumArt)
            if (MyPlayer.isPlaying) {
                playButton.setImageResource(R.drawable.pause)
            } else {
                playButton.setImageResource(R.drawable.play)
            }
        }
    }

    protected open fun pause() {
        playButton.setImageResource(R.drawable.play)
    }

    protected open fun play() {
        playButton.setImageResource(R.drawable.pause)
    }

    open fun reset() {
        pause()
    }
}