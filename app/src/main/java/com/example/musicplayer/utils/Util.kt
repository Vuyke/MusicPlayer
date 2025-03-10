package com.example.musicplayer.utils

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.musicplayer.R
import com.example.musicplayer.activities.MusicPlayerService
import com.example.musicplayer.song_ui.SongUI
import com.google.common.util.concurrent.ListenableFuture
import java.io.IOException

object Util {
    const val REQ = 100
    var bluetoothCommand: Boolean = true
    var songs: SongArray = SongArray()

    fun initSongs(context: Context) {
        songs = SongArray(context)
    }

    fun createListener(controller: Player, songUI: SongUI) = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying || bluetoothCommand || !controller.playWhenReady) {
                if(isPlaying)
                    Log.d("SongUI ", "IS PLAYING TRUE!!!")
                else
                    Log.d("SongUI", "IS PLAYING FALSE!!!")
                songUI.refresh()
            }
            bluetoothCommand = true
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d("SongUI", "MEDIA TRANSITION DEFAULT")
            songUI.refreshAlbumCover()
            if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                songUI.refresh()
                Log.d("SongUI", "MEDIA TRANSITION")
            }
        }
    }

    fun getController(context: Context): ListenableFuture<MediaController> {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlayerService::class.java))
        return MediaController.Builder(context, sessionToken).buildAsync()
    }
    fun readMediaRequest(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                REQ
            )
        }
        else {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQ
            )
        }
    }

    fun readMediaGranted(context : Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun postNotificationsRequest(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQ
            )
        }
    }

    fun postNotificationsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun getAlbumArt(albumUri: Uri?, context: Context): Bitmap? {
        if (albumUri == null) return null
        return try {
            context.contentResolver.loadThumbnail(
                albumUri,
                Size(288, 288),
                null
            )
        } catch (e: IOException) {
            println("Failed to load picture")
            null
        }
    }

    fun getImageView(albumUri: Uri?, context: Context, imageView: ImageView, iconPath: Int) {
        Glide.with(context)
            .load(albumUri)
            .placeholder(iconPath)
            .transform(RoundedCorners(16))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .thumbnail(0.1f)
            .into(imageView)
    }

    fun format(milli: Int): String {
        val minutes = milli / 60000
        val seconds = milli % 60000 / 1000
        var minString = minutes.toString()
        var secString = seconds.toString()
        if (minutes < 10) {
            minString = "0$minString"
        }
        if (seconds < 10) {
            secString = "0$secString"
        }
        return "$minString:$secString"
    }

    fun artistAlbumFormat(artist: CharSequence?, album: CharSequence?): String {
        return if (album != null)  "$artist - $album"
        else artist.toString()
    }
}