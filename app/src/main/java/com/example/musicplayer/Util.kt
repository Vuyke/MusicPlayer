package com.example.musicplayer

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

object Util {
    const val REQ = 100
    fun permissions(context : Context, activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                REQ)
        }
        else {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQ)
        }
    }

    fun arePermissionsGranted(context : Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun brightenColor(color: Int, factor: Float): Int {
        val r = ((Color.red(color) * factor).coerceAtMost(255f)).toInt()
        val g = ((Color.green(color) * factor).coerceAtMost(255f)).toInt()
        val b = ((Color.red(color) * factor).coerceAtMost(255f)).toInt()
        return Color.rgb(r, g, b)
    }

    fun mp3Files(context: Context): MutableList<MusicItem>{
        val list:MutableList<MusicItem> = mutableListOf()
        val contentResolver: ContentResolver = context.contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val collect: Array<String> = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media._ID
        )
        val selection = "${MediaStore.Audio.Media.MIME_TYPE} IN (?, ?) AND ${MediaStore.Audio.Media.DISPLAY_NAME }<> ?"
        val args: Array<String> = arrayOf("audio/mpeg", "audio/mp4", "tone.mp3")
        val cursor = contentResolver.query(uri, collect, selection, args, MediaStore.Audio.Media.DATE_ADDED)
        cursor?.use {
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdIndex = it.getColumnIndex(MediaStore.Audio.Media._ID)
            while(it.moveToNext()) {
                val id = it.getLong(albumIdIndex)
                val albumUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                list.add(MusicItem(it.getString(dataIndex), it.getString(artistIndex), it.getString(pathIndex), albumUri))
            }
        }
        return list.asReversed()
    }

    private fun getAlbumArt(albumUri: Uri?, context: Context): Bitmap? {
        if (albumUri == null)
            return null
        return try {
            context.contentResolver.loadThumbnail(
                albumUri,
                Size(300, 300),
                null
            )
        } catch (e: IOException) {
            println("Failed to load picture")
            null
        }
    }

    fun getImageView(albumUri: Uri?, context: Context, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = getAlbumArt(albumUri, context)
            if (bitmap == null) {
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.ic_launcher_background)
                }
            }
            else {
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }
}