package com.example.musicplayer.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.R
import com.example.musicplayer.utils.Util
import com.example.musicplayer.song_ui.SongLayout

class ActivityPlayingSong : AppCompatActivity() {

    private lateinit var albumArt: ImageView
    private lateinit var layout: SongLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_song_playing)
        layout = SongLayout(findViewById(R.id.main))
        layout.refresh()
        MyPlayer.setActiveSongUI(layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        albumArt = findViewById(R.id.albumArt)
        Util.getImageView(MyPlayer.music?.artPath, this, albumArt)
    }
}