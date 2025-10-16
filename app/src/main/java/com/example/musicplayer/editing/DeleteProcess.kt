package com.example.musicplayer.editing

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity

class DeleteProcess(activity: ComponentActivity, private val context: Context): AbstractProcess<Unit>(activity, context) {

    override fun dialogStart() {
        AlertDialog.Builder(context)
            .setTitle("Are you sure you want to delete ${song.mediaMetadata.title}?")
            .setPositiveButton("YES") { _, _ ->
                permissionRequest()
            }
            .setNegativeButton("No", null)
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onPermissionGranted() {
        try {
            val del = contentResolver.delete(uri, null, null)
            if (del > 0) {
                Log.d("File delete", "Success!")
                onSuccess(Unit)
            } else {
                Log.d("File delete", "Fail!")
            }
        } catch (e: Exception) {
            Log.d("File delete", "Error renaming file! ${e.message}")
        }
    }
}