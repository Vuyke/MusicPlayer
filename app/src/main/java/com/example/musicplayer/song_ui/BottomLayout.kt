package com.example.musicplayer.song_ui

import android.view.View
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.musicplayer.R

class BottomLayout(view: View) : SongUI(view) {
    private val bottom: ConstraintLayout = view.findViewById(R.id.bottomLayout)
    private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

    override fun updateDuration(duration: Long) {
        progressBar.max = duration.toInt()
    }

    override fun updateProgress(current: Long) {
        progressBar.progress = current.toInt()
    }

    fun setVisibility(visibility: Int) {
        bottom.visibility = visibility
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        bottom.setOnClickListener(listener)
    }
}