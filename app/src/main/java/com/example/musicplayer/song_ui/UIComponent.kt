package com.example.musicplayer.song_ui

import androidx.media3.common.MediaItem

interface UIComponent {
    fun refreshOnSongChange(song: MediaItem)

    fun refreshOnIsPlayingChange(isPlaying: Boolean)

    fun updateDuration(duration: Long)

    fun updateProgress(current: Long)
}