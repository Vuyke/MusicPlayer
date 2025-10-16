package com.example.musicplayer.activities

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.musicplayer.R
import com.example.musicplayer.song_ui.SongLayout
import com.example.musicplayer.view_model.SongViewModel

class ActivityPlayingSong : AppCompatActivity() {
    private val viewModel: SongViewModel by viewModels()
    private lateinit var layout: SongLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_playing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initLayout()
        viewModelLogic()
    }

    private fun initLayout() {
        layout = SongLayout(findViewById(R.id.main))
        layout.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var currentProgress = 0L
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProgress = progress.toLong()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                viewModel.playerPauseSong()
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.playerMoveSong(currentProgress)
                viewModel.playerPlaySong()
            }
        })
        layout.next.setOnClickListener {
            viewModel.playerPlayNext()
        }
        layout.previous.setOnClickListener {
            viewModel.playerPlayPrevious()
        }
        layout.shuffleMode.setOnClickListener {
            viewModel.playerChangeShuffleMode()
        }
        layout.playPauseButton.setOnClickListener {
            viewModel.playerChangeIsPlaying()
        }
    }

    private fun viewModelLogic() {
        viewModel.song.observe(this) { song ->
            layout.refreshOnSongChange(song)
        }
        viewModel.isPlaying.observe(this) { isPlaying ->
            layout.refreshOnIsPlayingChange(isPlaying)
        }
        viewModel.shuffleMode.observe(this) { shuffleMode ->
            val resource = if (shuffleMode) R.drawable.media3_icon_shuffle_on else R.drawable.media3_icon_shuffle_off
            layout.shuffleMode.setImageResource(resource)
        }
        viewModel.progress.observe(this) { progress ->
            layout.updateProgress(progress)
        }
        viewModel.duration.observe(this) { duration ->
            layout.updateDuration(duration)
        }
    }

}