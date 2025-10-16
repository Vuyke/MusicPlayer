package com.example.musicplayer.song_ui

import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.media3.common.MediaItem
import com.example.musicplayer.R
import com.example.musicplayer.views.Trimmer

class EditUI(view: View): UIComponent {
    val bar: Trimmer = view.findViewById(R.id.trimmer)
    val playPauseButton: ImageView = view.findViewById(R.id.play_pause_button)
    val exportButton: Button = view.findViewById(R.id.exportButton)

    override fun updateDuration(duration: Long) {
        bar.setAudioDuration(duration.toFloat())
    }

    override fun updateProgress(current: Long) {
        bar.setPlaybackPosition(current.toFloat())
    }

    override fun refreshOnSongChange(song: MediaItem) {}

    override fun refreshOnIsPlayingChange(isPlaying: Boolean) {
        playPauseButton.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
    }
}