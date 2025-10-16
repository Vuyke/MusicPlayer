package com.example.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.IOException

object Util {
    var songs: SongArray = SongArray()

    fun initSongs(context: Context) {
        if (songs.size == 0) {
            songs = SongArray(context)
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
            Log.d("Application error","Failed to load picture")
            null
        }
    }

    fun getImageView(albumUri: Uri?, context: Context, imageView: ImageView, iconPath: Int) {
        Glide.with(context)
            .load(albumUri)
            .placeholder(iconPath)
            .transform(RoundedCorners(16))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    fun format(milli: Long): String {
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

    fun logMediaItemInfo(index: Int) {
        val mediaItem = songs.get(index)
        Log.d("MediaItem", "$index, ${songs.getIndex(index)}")
        Log.d("MediaItem", "Title: ${mediaItem.mediaMetadata.title}")
        Log.d("MediaItem", "ID: ${mediaItem.mediaId}")
        Log.d("MediaItem", "Uri: ${mediaItem.localConfiguration?.uri}")
    }
}