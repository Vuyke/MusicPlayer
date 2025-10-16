package com.example.musicplayer.song_ui

import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.utils.Util

class SongLayout(view: View): SongUI(view) {
    val seekBar: SeekBar = view.findViewById(R.id.seekBar)
    val next: ImageView = view.findViewById(R.id.next_button)
    val previous: ImageView = view.findViewById(R.id.previous_button)
    private val songProgress: TextView = view.findViewById(R.id.currentDuration)
    private val durationTextView: TextView = view.findViewById(R.id.duration)
    val shuffleMode: ImageView = view.findViewById(R.id.shuffle)

    override fun updateDuration(duration: Long) {
        seekBar.max = duration.toInt()
        durationTextView.text = Util.format(duration)
    }

    override fun updateProgress(current: Long) {
        seekBar.progress = current.toInt()
        songProgress.text = Util.format(current)
    }
}