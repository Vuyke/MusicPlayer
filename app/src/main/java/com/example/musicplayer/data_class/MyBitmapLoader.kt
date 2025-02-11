package com.example.musicplayer.data_class

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.example.musicplayer.R
import com.example.musicplayer.utils.Util
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@UnstableApi
class MyBitmapLoader(private val context: Context) : BitmapLoader {
    override fun supportsMimeType(mimeType: String): Boolean {
        return true
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        return Futures.immediateFuture(BitmapFactory.decodeByteArray(data, 0, data.size))
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        return Futures.immediateFuture(Util.getAlbumArt(uri, context))
    }
}