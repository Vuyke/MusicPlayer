package com.example.musicplayer.song_ui

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.musicplayer.R
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.utils.Util

class BottomLayout(view: View, player: MyPlayer?) : SongUI(view, player) {
    val bottom: ConstraintLayout = view.findViewById(R.id.bottomLayout)
    private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

    override fun updateDuration() {
        player?.let {
            progressBar.max = it.duration
            progressBar.progress = it.currentPosition
        }
    }

    override fun updateProgress(current: Int) {
        progressBar.progress = current
    }
}