package com.example.musicplayer.editing

import androidx.media3.common.MediaItem

interface EditProcess<T> {
    fun startProcess(song: MediaItem, onSuccess: (T) -> Unit  = {})
    fun permissionRequest()
}