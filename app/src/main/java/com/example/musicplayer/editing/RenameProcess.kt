package com.example.musicplayer.editing

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class RenameProcess(activity: ComponentActivity, private val context: Context): AbstractProcess<String>(activity, context) {
    private var newName = ""

    override fun dialogStart() {
        val input = EditText(context)
        input.hint = "Enter new name"
        input.setText(song.mediaMetadata.title)
        val dialog = AlertDialog.Builder(context)
            .setTitle("Rename")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                newName = input.text.toString()
                if (newName.isNotEmpty()) {
                    Log.d("File rename", song.mediaId)
                    permissionRequest()
                } else {
                    Toast.makeText(context, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    override fun onPermissionGranted() {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.IS_PENDING, 1)
                put(MediaStore.Audio.Media.DISPLAY_NAME, newName)
                put(MediaStore.Audio.Media.TITLE, newName)
                put(MediaStore.Audio.Media.IS_PENDING, 0)
            }
            val upd = contentResolver.update(uri, values, null, null)
            if (upd > 0) {
                Log.d("File Rename", "Success!")
                onSuccess(newName)
            } else {
                Log.d("File Rename", "Fail!")
            }
        } catch (e: Exception) {
            Log.d("File Rename", "Error renaming file! ${e.message}")
        }
    }

}