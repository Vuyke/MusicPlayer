package com.example.musicplayer.song_ui

import android.content.ContentValues.TAG
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicplayer.data_class.MyBitmapLoader
import com.example.musicplayer.utils.MyPlayer

class MusicPlayerService : MediaSessionService() {
//    private val customCommandPrev = SessionCommand(PREV, Bundle.EMPTY)
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, MyPlayer.player)
            .setBitmapLoader(MyBitmapLoader(applicationContext))
            .setCallback(sessionCallback)
            .build()
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
//        val notification  = NotificationCompat.Builder(this, "5")
//            .setLargeIcon()
//    }
}