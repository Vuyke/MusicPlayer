package com.example.musicplayer.song_ui

import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.R
import com.example.musicplayer.data_class.PlayType

class SongLayout(view: View): SongUI(view) {
    private val seekBar: SeekBar = view.findViewById(R.id.seekBar)
    private val next: ImageView = view.findViewById(R.id.next_button)
    private val previous: ImageView = view.findViewById(R.id.previous_button)
    override val format = R.drawable.launch_icon

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
                handler.post(updateProgressBar)
                MyPlayer.songMove(currentProgress)
            }
        })
        next.setOnClickListener {
            MyPlayer.startSong(PlayType.NEXT)
        }
        previous.setOnClickListener {
            MyPlayer.startSong(PlayType.PREV)
        }
    }

    override fun updateDuration() {
        seekBar.max = MyPlayer.duration
        seekBar.progress = MyPlayer.currentPosition
    }

    override fun updateProgress(current: Int) {
        seekBar.progress = current
    }
}