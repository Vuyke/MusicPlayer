package com.example.musicplayer.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.data_class.PlayType
import com.example.musicplayer.song_ui.BottomLayout
import com.example.musicplayer.utils.ItemRecycleAdapter
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var recycle: RecyclerView
    private lateinit var bottom: BottomLayout
    private lateinit var listener: Player.Listener
    private lateinit var search: EditText
    private var player: MyPlayer? = null
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        if(Util.readMediaGranted(this))
            onStartLogic()
        else
            Util.readMediaRequest(this)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun onStartLogic() {
        if (!Util.postNotificationsGranted(this)) {
            Util.postNotificationsRequest(this)
        }
        Util.initSongs(this)
        setContentView(R.layout.activity_main)
        initVariables()
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//should delete
        recycle = findViewById(R.id.recycle1)
        recycle.setItemViewCacheSize(20)
        recycle.layoutManager = LinearLayoutManager(this)
        recycle.setAdapter(ItemRecycleAdapter(this, Util.songs,  { song, i ->
            player?.let {
                startForeground()
                Util.bluetoothCommand = false
                it.initQueue(i)
                it.startSong(PlayType.CURRENT)
                bottom.bottom.visibility = ConstraintLayout.VISIBLE
                Log.d("File", "Current song uri:${song.localConfiguration?.uri}")
            }
        }, {song, i -> options(song, i)}))
        search = findViewById(R.id.searchSong)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(250)
                    withContext(Dispatchers.IO) {
                        Util.songs.updateSearch(s.toString())
                    }
                    recycle.adapter?.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "OnStart started")
    }

    private fun initVariables() {
        val future = Util.getController(this)
        future.addListener({
            try {
                val controller = future.get()
                val tempPlayer = MyPlayer(controller)
                player = tempPlayer
                bottom = BottomLayout(findViewById(R.id.main), tempPlayer)
                listener = Util.createListener(controller, bottom)
                controller.addListener(listener)
                bottom.bottom.setOnClickListener {
                    val intent = Intent(this, ActivityPlayingSong::class.java)
                    val animations = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        android.util.Pair(bottom.albumArt, "album_cover_transition"),
                        android.util.Pair(bottom.artist, "artist_transition")
                    )
                    startActivity(intent, animations.toBundle())
                }
            } catch (e: Exception) {
                Log.e("File", "Error getting MediaController", e)
                // Handle error, e.g., show a message to the user
            }
        }, ContextCompat.getMainExecutor(this)) // Use main executor
    }

    private fun startForeground() {
        val serviceIntent = Intent(this, MusicPlayerService::class.java)
        startForegroundService(serviceIntent)
    }

    override fun onPause() {
        super.onPause()
        player?.player?.removeListener(listener)
    }

    override fun onResume() {
        super.onResume()
        player?.player?.let {
            it.removeListener(listener)
            it.addListener(listener)
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart")
        bottom.refresh(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        player?.destroy()
        stopService(Intent(this, MusicPlayerService::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Util.REQ) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onStartLogic()
            }
            else {
                Toast.makeText(this, "Permissions are required to proceed", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private lateinit var newName: String
    private lateinit var uri: Uri
    private var index = -1

    private fun options(song: MediaItem, ind: Int) {
        val options = arrayOf("Delete", "Rename", "Edit")
        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when(options[which]) {
                    "Rename" -> renameWindow(song, ind)
                    "Delete" -> deleteWindow(song, ind)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun renameWindow(song: MediaItem, ind: Int) {
        val input = EditText(this)
        input.hint = "Enter new name"
        input.setText(song.mediaMetadata.title)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Rename")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotEmpty()) {
                    Log.d("File rename", song.mediaId)
                    changeSongName(Uri.parse(song.mediaId), newName, ind)
                } else {
                    Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
//        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
//        input.selectAll()
    }

    private fun deleteWindow(song: MediaItem, ind: Int) {
        AlertDialog.Builder(this)
            .setTitle("Are you sure you want to delete ${song.mediaMetadata.title}?")
            .setPositiveButton("YES") { _, _ ->
                deleteSong(Uri.parse(song.mediaId), ind)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteSong(uri: Uri?, ind: Int) {
        if(uri == null) return
        this.uri = uri
        this.index = ind
        val contentResolver = contentResolver
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                deleteContract.launch(
                    IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                )
            } catch(e: Exception) {
                Toast.makeText(this, "Error initiating deletion: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun changeSongName(uri: Uri?, newName: String, ind: Int) {
        if(uri == null) return
        this.uri = uri
        this.newName = newName
        this.index = ind
        val contentResolver = contentResolver
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                renameContract.launch(
                    IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                )
            } catch(e: Exception) {
                Toast.makeText(this, "Error initiating rename: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val renameContract = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) {result ->
        if(result.resultCode == Activity.RESULT_OK) {
            Log.d("File", "Rename process started for:${uri} on ${uri.path}")
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
                    Util.songs.updateName(index, newName)
                    recycle.adapter?.notifyItemChanged(index)
                } else {
                    Log.d("File Rename", "Fail!")
                }
            } catch (e: Exception) {
                Log.d("File Rename", "Error renaming file! ${e.message}")
            }
        }
    }

    private val deleteContract = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) {result ->
        if(result.resultCode == Activity.RESULT_OK) {
            Log.d("File", "Delete process started for:${uri} on ${uri.path}")
            try {
                val del = contentResolver.delete(uri, null, null)
                if (del > 0) {
                    Log.d("File Delete", "Success!")
                    Util.songs.remove(index)
                    recycle.adapter?.notifyItemRemoved(index)
                } else {
                    Log.d("File Rename", "Fail!")
                }
            } catch (e: Exception) {
                Log.d("File Rename", "Error renaming file! ${e.message}")
            }
        }
    }
}