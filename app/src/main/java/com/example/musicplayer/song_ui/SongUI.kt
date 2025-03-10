package com.example.musicplayer.song_ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.R
import com.example.musicplayer.utils.Util

abstract class SongUI(view: View, val player: MyPlayer?) {
    val format: Int = R.drawable.launch_icon
    val artist: TextView = view.findViewById(R.id.artist)
    private val song: TextView = view.findViewById(R.id.song)
    private val playButton: ImageView = view.findViewById(R.id.play_pause_button)
    val albumArt: ImageView = view.findViewById(R.id.albumArt)
    val context: Context = view.context//should be private
    protected val handler = Handler(Looper.getMainLooper())
    protected val updateProgressBar = object : Runnable {
        override fun run() {
            player?.let {
                updateProgress(it.currentPosition)
                handler.postDelayed(this, 500)
            }
        }
    }

    init {
        playButton.setOnClickListener {
            changeState()
        }
    }

    fun refresh(albumCoverUpdate: Boolean = false) {
        player?.music?.let {
            Log.d("SongUI", "refresh occurred")
            artist.text = Util.artistAlbumFormat(it.mediaMetadata.artist, it.mediaMetadata.albumTitle)
            song.text = it.mediaMetadata.title
            playButton.setImageResource(player.playPauseResource())
            if(albumCoverUpdate)
                refreshAlbumCover()
            if (player.isPlaying)
                handler.post(updateProgressBar)
            else
                handler.removeCallbacks(updateProgressBar)
            updateDuration()
        }
    }

    fun refreshAlbumCover() {
        player?.music?.let {
            Util.getImageView(it.mediaMetadata.artworkUri, context, albumArt, format)
        }
    }

    abstract fun updateDuration()

    abstract fun updateProgress(current: Int)

    private fun changeState() {
        player?.let {
            if (player.isPlaying)
                player.pauseSong()
            else
                player.playSong()
        }
    }
}