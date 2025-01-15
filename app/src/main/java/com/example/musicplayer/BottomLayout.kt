package com.example.musicplayer

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout

class BottomLayout(view: View) : SongUI(view) {
    val bottom: ConstraintLayout = view.findViewById(R.id.bottomLayout)
    private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressBar = object : Runnable {
        override fun run() {
            progressBar.progress = MyPlayer.currentPosition
            handler.postDelayed(this, 750)
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
            if (bottom.visibility != ConstraintLayout.VISIBLE) {
                bottom.visibility = ConstraintLayout.VISIBLE
            }
            progressBar.max = MyPlayer.duration
            progressBar.progress = MyPlayer.currentPosition
            handler.post(updateProgressBar)
        }
    }
}