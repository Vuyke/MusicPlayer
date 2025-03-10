package com.example.musicplayer.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.R
import com.example.musicplayer.song_ui.BottomLayout
import com.example.musicplayer.utils.Util
import com.example.musicplayer.song_ui.SongLayout

class ActivityPlayingSong : AppCompatActivity() {

    private lateinit var layout: SongLayout
    private var player: MyPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_song_playing)
        val future = Util.getController(this)
        future.addListener({
            try {
                val mediaController = future.get()
                val tempPlayer = MyPlayer(mediaController)
                layout = SongLayout(findViewById(R.id.main), tempPlayer)
                layout.refresh(true)
                mediaController.addListener(Util.createListener(mediaController, layout))
                player = tempPlayer
            } catch (e: Exception) {
                Log.e("File", "Error getting MediaController", e)
                // Handle error, e.g., show a message to the user
            }
        }, ContextCompat.getMainExecutor(this))
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy()
    }
}