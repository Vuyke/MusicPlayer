package com.example.musicplayer.song_ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.example.musicplayer.R
import com.example.musicplayer.utils.Util

abstract class SongUI(private val view: View): UIComponent {
    private val format: Int = R.drawable.launch_icon
    val artist: TextView = view.findViewById(R.id.artist)
    private val songName: TextView = view.findViewById(R.id.songName)
    val playPauseButton: ImageView = view.findViewById(R.id.play_pause_button)
    val albumArt: ImageView = view.findViewById(R.id.albumArt)

    @OptIn(UnstableApi::class)
    override fun refreshOnSongChange(song: MediaItem) {
        artist.text = Util.artistAlbumFormat(song.mediaMetadata.artist, song.mediaMetadata.albumTitle)
        songName.text = song.mediaMetadata.title
        Util.getImageView(song.mediaMetadata.artworkUri, view.context, albumArt, format)
        updateDuration(song.mediaMetadata.durationMs?.div(1000) ?: -1)
    }

    override fun refreshOnIsPlayingChange(isPlaying: Boolean) {
        playPauseButton.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
    }
}