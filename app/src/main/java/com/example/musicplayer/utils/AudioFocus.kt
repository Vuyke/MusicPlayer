package com.example.musicplayer.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

class AudioFocus(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!MyPlayer.isPlaying) {
                    MyPlayer.forcePlaySong()
                }
                MyPlayer.setVolume(1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (MyPlayer.isPlaying) {
                    MyPlayer.forcePauseSong()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                MyPlayer.setVolume(0.2f)
            }
        }
    }

    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(audioFocusListener)
            .build()

    fun requestFocus(): Boolean {
        return audioManager.requestAudioFocus(audioFocusRequest) ==
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun abandonFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }
}