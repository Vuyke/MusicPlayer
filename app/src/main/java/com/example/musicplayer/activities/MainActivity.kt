package com.example.musicplayer.activities

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.viewModel.SongViewModel
import com.example.musicplayer.song_ui.BottomLayout
import com.example.musicplayer.editing.DeleteProcess
import com.example.musicplayer.utils.ItemRecycleAdapter
import com.example.musicplayer.editing.RenameProcess
import com.example.musicplayer.utils.PermissionUtils
import com.example.musicplayer.utils.Util

class MainActivity : AppCompatActivity() {
    private val viewModel: SongViewModel by viewModels()
    private lateinit var recycle: RecyclerView
    private lateinit var bottomLayout: BottomLayout
    private lateinit var search: EditText
    private lateinit var deleteProcess: DeleteProcess
    private lateinit var renameProcess: RenameProcess

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deleteProcess = DeleteProcess(this, this)
        renameProcess = RenameProcess(this, this)
        if(PermissionUtils.readMediaGranted(this))
            onCreateLogic()
        else
            PermissionUtils.readMediaRequest(this)
    }

    private fun onCreateLogic() {
        if (!PermissionUtils.postNotificationsGranted(this)) {
            PermissionUtils.postNotificationsRequest(this)
        }
        Util.initSongs(this)
        setContentView(R.layout.activity_main)
        initRecycle()
        initBottom()
        initSearch()
        viewModelLogic()
    }

    private fun viewModelLogic() {
        viewModel.song.observe(this) { song ->
            bottomLayout.refreshOnSongChange(song)
        }
        viewModel.isPlaying.observe(this) { isPlaying ->
            bottomLayout.refreshOnIsPlayingChange(isPlaying)
        }
        viewModel.progress.observe(this) { progress ->
            bottomLayout.updateProgress(progress)
        }
        viewModel.somethingPlayed.observe(this) { somethingPlayed ->
            if (somethingPlayed) {
                bottomLayout.setVisibility(View.VISIBLE)
            }
        }
        viewModel.duration.observe(this) { duration ->
            bottomLayout.updateDuration(duration)
        }
    }

    private fun initRecycle() {
        recycle = findViewById(R.id.recycleViewForSongs)
        recycle.layoutManager = LinearLayoutManager(this)
        recycle.setAdapter(ItemRecycleAdapter(this, Util.songs,  { song, i ->
            viewModel.playerSeekSong(i)
            startNotificationPlayer()
            Log.d("Song", "Current song uri:${song.localConfiguration?.uri}, index: $i")
        }, {song, i -> options(song, i)}))
    }

    private fun initSearch() {
        search = findViewById(R.id.searchSong)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.searchUpdated(text.toString())
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun initBottom() {
        bottomLayout = BottomLayout(findViewById(R.id.main))
        bottomLayout.playPauseButton.setOnClickListener {
            viewModel.playerChangeIsPlaying()
        }
        bottomLayout.setOnClickListener {
            val intent = Intent(this, ActivityPlayingSong::class.java)
            val animations = ActivityOptions.makeSceneTransitionAnimation(
                this,
                android.util.Pair(bottomLayout.albumArt, "album_cover_transition"),
                android.util.Pair(bottomLayout.artist, "artist_transition")
            )
            startActivity(intent, animations.toBundle())
        }
    }

    private fun startNotificationPlayer() {
        val serviceIntent = Intent(this, MusicPlayerService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun options(song: MediaItem, ind: Int) {
        val options = arrayOf("Delete", "Rename", "Edit")
        AlertDialog.Builder(this).setItems(options) { _, which ->
            when(options[which]) {
                "Rename" -> renameProcess.startProcess(song) { newName ->
                    Util.songs.updateName(ind, newName)
                    viewModel.playerUpdateSong(Util.songs.get(ind), Util.songs.getIndex(ind))
                    recycle.adapter?.notifyItemChanged(ind)
                }
                "Delete" -> deleteProcess.startProcess(song) {
                    viewModel.playerDeleteSong(Util.songs.getIndex(ind))
                    Util.songs.remove(ind)
                    recycle.adapter?.notifyDataSetChanged()
                }
                "Edit" -> editWindow(ind)
            }
        }
        .setNegativeButton("Cancel", null)
        .show()
    }


    private fun editWindow(ind: Int) {
        viewModel.playerPauseSong()
        val intent = Intent(this, EditFileActivity::class.java).apply {
            putExtra("songIndex", ind)
        }
        startActivityForResult(intent, PermissionUtils.REQ_TRIM)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, MusicPlayerService::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.REQ) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onCreateLogic()
            }
            else {
                Toast.makeText(this, "Permissions are required to proceed", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionUtils.REQ_TRIM && resultCode == RESULT_OK) {
            val result = data?.getStringExtra("state")
            if (result == "success") {
                Toast.makeText(this, "Trim successful, restart for update", Toast.LENGTH_SHORT).show()
            }
        }
    }
}