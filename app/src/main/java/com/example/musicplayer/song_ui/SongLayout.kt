package com.example.musicplayer.song_ui

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.R
import com.example.musicplayer.data_class.PlayType
import com.example.musicplayer.utils.Util

class SongLayout(view: View, player: MyPlayer?): SongUI(view, player) {
    private val seekBar: SeekBar = view.findViewById(R.id.seekBar)
    private val next: ImageView = view.findViewById(R.id.next_button)
    private val previous: ImageView = view.findViewById(R.id.previous_button)
    private val songDuration: TextView = view.findViewById(R.id.currentDuration)
    private val duration: TextView = view.findViewById(R.id.duration)

    init {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var currentProgress = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) currentProgress = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateProgressBar)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Util.bluetoothCommand = false
                handler.post(updateProgressBar)
                player?.songMove(currentProgress)
            }
        })
        next.setOnClickListener {
            player?.startSong(PlayType.NEXT)
        }
        previous.setOnClickListener {
            player?.startSong(PlayType.PREV)
        }
    }

    override fun updateDuration() {
        player?.let {
            seekBar.max = it.duration
            seekBar.progress = it.currentPosition
            duration.text = Util.format(it.duration)
        }
    }

    override fun updateProgress(current: Int) {
        seekBar.progress = current
        songDuration.text = Util.format(current)
    }

}