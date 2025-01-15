package com.example.musicplayer

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar

class SongLayout(view: View): SongUI(view) {
    private val seekBar: SeekBar = view.findViewById(R.id.seekBar)
    private val next: ImageView = view.findViewById(R.id.next_button)
    private val previous: ImageView = view.findViewById(R.id.previous_button)
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressBar = object : Runnable {
        override fun run() {
            seekBar.progress = MyPlayer.currentPosition
            handler.postDelayed(this, 750)
        }
    }

    init {
        seekBar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    MyPlayer.moveSong(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        next.setOnClickListener {
            MyPlayer.startSong(context, MyPlayer.nextSong())
        }
        previous.setOnClickListener {
            MyPlayer.startSong(context, MyPlayer.previousSong())
        }
    }

    override fun pause() {
        super.pause()
        handler.removeCallbacks(updateProgressBar)
    }

    override fun play() {
        super.play()
        handler.post(updateProgressBar)
    }

    override fun reset() {
        pause()
    }

    override fun refresh() {
        super.refresh()
        if (MyPlayer.music != null) {
            seekBar.max = MyPlayer.duration
            seekBar.progress = MyPlayer.currentPosition
            if (MyPlayer.isPlaying)
                handler.post(updateProgressBar)
            else
                handler.removeCallbacks(updateProgressBar)
        }
    }
}