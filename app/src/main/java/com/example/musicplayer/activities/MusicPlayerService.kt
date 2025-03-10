package com.example.musicplayer.activities

import android.content.ContentValues.TAG
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicplayer.data_class.MyBitmapLoader
import com.example.musicplayer.utils.Util

class MusicPlayerService : MediaSessionService() {
//    private val customCommandPrev = SessionCommand(PREV, Bundle.EMPTY)
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        val player: ExoPlayer = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player)
            .setBitmapLoader(MyBitmapLoader(applicationContext))
            .setCallback(sessionCallback)
            .build()
        mediaSession?.player?.let {
            it.setAudioAttributes(audioAttributes, true)
            it.addMediaItems(Util.songs.getMediaItems())
            it.prepare()
        }
        Log.d(TAG, "Created service")
    }

    private val sessionCallback = object : MediaSession.Callback{}

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession
}