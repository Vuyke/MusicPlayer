package com.example.musicplayer.data_class

import androidx.media3.common.MediaItem

class MusicItem(val item: MediaItem) {
    val name = item.mediaMetadata.title
    val artist = item.mediaMetadata.artist
    val path = item.mediaId
    val artPath = item.mediaMetadata.artworkUri
}