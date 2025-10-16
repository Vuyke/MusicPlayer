package com.example.musicplayer.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import com.example.musicplayer.R
import com.example.musicplayer.editing.TrimProcess
import com.example.musicplayer.song_ui.EditUI
import com.example.musicplayer.utils.Util
import com.example.musicplayer.viewModel.EditSongViewModel

class EditFileActivity : AppCompatActivity() {
    private val viewModel: EditSongViewModel by viewModels()
    private var songIndex = 0
    private lateinit var ui: EditUI
    private lateinit var song: MediaItem
    private lateinit var trimProcess: TrimProcess

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_file)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initUI()
        viewModelLogic()
        songIndex = intent.getIntExtra("songIndex", 0)
        song = Util.songs.get(songIndex)
        trimProcess = TrimProcess(this, this)
        viewModel.initializePlayer(songIndex)
    }

    private fun initUI() {
        ui = EditUI(findViewById(R.id.main))
        ui.exportButton.setOnClickListener {
            trimProcess.setInterval(ui.bar.start().toLong(), ui.bar.end().toLong())
            trimProcess.startProcess(song) {
                val intent = Intent()
                intent.putExtra("state", "success")
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        ui.bar.setOnRangeChangeListener { slider, _, _ ->
            viewModel.updateSlider(slider.values[0], slider.values[1])
        }
        ui.playPauseButton.setOnClickListener {
            viewModel.playerChangeIsPlaying()
        }
    }

    private fun viewModelLogic() {
        viewModel.progress.observe(this) { progress ->
            ui.bar.setPlaybackPosition(progress.toFloat())
        }
        viewModel.isPlaying.observe(this) { isPlaying ->
            ui.refreshOnIsPlayingChange(isPlaying)
        }
        viewModel.duration.observe(this) { duration ->
            ui.bar.setAudioDuration(duration.toFloat())
        }
    }
}