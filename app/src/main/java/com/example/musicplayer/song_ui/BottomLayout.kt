package com.example.musicplayer.song_ui

import android.view.View
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.musicplayer.R
import com.example.musicplayer.utils.MyPlayer

class BottomLayout(view: View) : SongUI(view) {
    val bottom: ConstraintLayout = view.findViewById(R.id.bottomLayout)
    private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
    override val format = R.mipmap.ic_launcher

    override fun updateDuration() {
        progressBar.max = MyPlayer.duration
        progressBar.progress = MyPlayer.currentPosition
        if (bottom.visibility != ConstraintLayout.VISIBLE) {
            bottom.visibility = ConstraintLayout.VISIBLE
        }
    }

    override fun updateProgress(current: Int) {
        progressBar.progress = current
    }
}