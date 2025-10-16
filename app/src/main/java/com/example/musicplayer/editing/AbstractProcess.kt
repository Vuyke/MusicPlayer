package com.example.musicplayer.editing

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.common.MediaItem

abstract class AbstractProcess<T>(activity: ComponentActivity, private val context: Context) : EditProcess<T> {
    protected lateinit var song: MediaItem
    protected lateinit var uri: Uri
    protected var index = 0
    protected var path: String? = null
    protected val contentResolver: ContentResolver = context.contentResolver
    protected lateinit var onSuccess: (T) -> Unit

    override fun startProcess(
        song: MediaItem,
        onSuccess: (T) -> Unit
    ) {
        this.song = song
        this.uri = Uri.parse(song.mediaId)
        this.path = song.localConfiguration?.uri?.path
        this.onSuccess = onSuccess
        dialogStart()
    }

    override fun permissionRequest() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                contract.launch(
                    IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                )
            } catch(e: Exception) {
                Toast.makeText(context, "Error with permission request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val contract = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onPermissionGranted()
        }
    }

    abstract fun dialogStart()
    abstract fun onPermissionGranted()
}