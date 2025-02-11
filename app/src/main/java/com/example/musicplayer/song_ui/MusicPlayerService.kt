package com.example.musicplayer.song_ui

import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.musicplayer.R
import com.example.musicplayer.activities.MainActivity
import com.example.musicplayer.data_class.MyBitmapLoader
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.utils.Util
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

const val PREV = "prev"
const val NEXT = "next"
const val PLAY_PAUSE = "play_pause"

class MusicPlayerService : MediaSessionService() {
//    private val customCommandPrev = SessionCommand(PREV, Bundle.EMPTY)
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
//        setMediaNotificationProvider(notificationProvider())
        mediaSession = MediaSession.Builder(this, MyPlayer.player)
            .setBitmapLoader(MyBitmapLoader(applicationContext))
            .setCallback(sessionCallback)
            .build()
        Log.d(TAG, "Created service")
    }

    private val sessionCallback = object : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            return super.onAddMediaItems(mediaSession, controller, mediaItems)
        }
    }

    @OptIn(UnstableApi::class)
    private fun notificationProvider(): DefaultMediaNotificationProvider {
        val provider = DefaultMediaNotificationProvider(this)
        return provider
    }


    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession?
        = mediaSession

//    private inner class MyCallBack : MediaSession.Callback {
//        @OptIn(UnstableApi::class)
//        override fun onConnect(
//            session: MediaSession,
//            controller: MediaSession.ControllerInfo
//        ): MediaSession.ConnectionResult {
//            return AcceptedResultBuilder(session)
//                .setAvailablePlayerCommands(
//                    ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
////                        .remove(Player.COMMAND_SEEK_TO_NEXT)
////                        .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
////                        .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
////                        .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
//                        .build()
//                )
//                .setAvailableSessionCommands(
//                    ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
////                        .add(customCommandPrev)
//                        .build()
//                )
//                .build()
//        }
//
////        override fun onCustomCommand(
////            session: MediaSession,
////            controller: MediaSession.ControllerInfo,
////            customCommand: SessionCommand,
////            args: Bundle
////        ): ListenableFuture<SessionResult> {
////            if(customCommand.customAction == PREV) {
////                MyPlayer.startSong(this@MusicPlayerService, MyPlayer.PlayType.PREV)
////                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
////            }
////            return super.onCustomCommand(session, controller, customCommand, args)
////        }
//    }

//    private fun clickIntent() : PendingIntent {
//        val notificaton  = NotificationCompat.Builder(this, "5")
//            .setLargeIcon()
//    }
}