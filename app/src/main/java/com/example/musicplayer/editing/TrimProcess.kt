package com.example.musicplayer.editing

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File

class TrimProcess (activity: ComponentActivity, private val context: Context): AbstractProcess<Unit>(activity, context) {

    private var start = 0L
    private var end = 0L

    fun setInterval(start: Long, end: Long) {
        this.start = start
        this.end = end
    }

    override fun dialogStart() {
        AlertDialog.Builder(context)
            .setTitle("Are you sure you want to trim ${song.mediaMetadata.title}?")
            .setPositiveButton("YES") { _, _ ->
                permissionRequest()
            }
            .setNegativeButton("No", null)
            .show()
    }

    @OptIn(UnstableApi::class)
    override fun onPermissionGranted() {
        try {
            trimAudio()
        } catch(e: Exception) {
            Log.d("Transformer", "Error transforming the file")
        }
    }

    @OptIn(UnstableApi::class)
    private fun createTransformer(originalFile: File, tempFile: File): Transformer {
        return Transformer.Builder(context)
            .addListener(object: Transformer.Listener {
                override fun onCompleted(composition: Composition, result: ExportResult) {
                    Log.d("Trimming", "Successfully written")
                    val newFile = generateUniqueFile(File(Environment.DIRECTORY_MUSIC), originalFile.nameWithoutExtension, originalFile.extension)
                    val values = ContentValues().apply {
                        put(MediaStore.Audio.Media.DISPLAY_NAME, "trimmed_${newFile.nameWithoutExtension}")
                        put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                        put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
                    }

                    val tempUri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                    tempUri?.let {
                        context.contentResolver.openOutputStream(it)?.use { out ->
                            tempFile.inputStream().use { input ->
                                input.copyTo(out)
                            }
                        }
                        Log.d("Trimming", "Output to new location completed!")
                        onSuccess(Unit)
                    }
                }

                override fun onError(composition: Composition, result: ExportResult, exception: ExportException) {
                    Log.d("Trimming", exception.message.toString())
                }
            })
            .build()
    }

    @OptIn(UnstableApi::class)
    private fun trimAudio() {
        path?.let{
            val originalFile = File(it)
            val tempFile = File(context.cacheDir, "trimmed_${originalFile.name}")
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setClippingConfiguration(ClippingConfiguration.Builder()
                    .setStartPositionMs(start)
                    .setEndPositionMs(end)
                    .build())
                .build()
            createTransformer(originalFile, tempFile).start(mediaItem, tempFile.absolutePath)
        }
    }

    private fun generateUniqueFile(dir: File?, baseName: String, extension: String): File {
        var file = File(dir, "$baseName.$extension")
        var counter = 1

        while (file.exists()) {
            file = File(dir, "$baseName($counter).$extension")
            counter++
        }
        Log.d("Trimming", file.path)
        return file
    }
}