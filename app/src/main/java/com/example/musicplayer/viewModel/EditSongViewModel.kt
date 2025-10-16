package com.example.musicplayer.viewModel

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.utils.Util

class EditSongViewModel(app: Application): AbstractPlayerViewModel(app) {
    private var songIndex: Int = 0
    private var cropSongStart: Float = 0f
    private var cropSongEnd: Float = 0f
    private var playerInitialized = false

    fun initializePlayer(ind: Int) {
        if (playerInitialized)
            return
        playerInitialized = true
        songIndex = ind
        val song = Util.songs.get(songIndex)
        val tempPlayer = ExoPlayer.Builder(getApplication()).build()
        tempPlayer.let {
            it.addMediaItem(song)
            it.prepare()
            it.addListener(createListener())
        }
        player = MyPlayer(tempPlayer)
        playerSeekSong()
    }

    fun updateSlider(start: Float, end: Float) {
        if (player.currentPosition < start) {
            player.songMove(start.toLong())
            _progress.postValue(cropSongStart.toLong())
        }
        if (player.currentPosition > end) {
            player.songMove(end.toLong())
            _progress.postValue(cropSongEnd.toLong())
        }
        cropSongEnd = end
        cropSongStart = start
    }

    override fun observeProgressAdditional() {
        if (player.currentPosition >= cropSongEnd) {
            player.pauseSong()
            player.songMove(cropSongStart.toLong())
            _progress.postValue(cropSongStart.toLong())
        }
    }
}