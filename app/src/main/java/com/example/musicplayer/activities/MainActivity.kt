package com.example.musicplayer.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.song_ui.BottomLayout
import com.example.musicplayer.song_ui.MusicPlayerService
import com.example.musicplayer.utils.ItemRecycleAdapter
import com.example.musicplayer.utils.MyPlayer
import com.example.musicplayer.utils.Util
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : AppCompatActivity() {
    private lateinit var recycle: RecyclerView
    private lateinit var bottom: BottomLayout
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var sessionToken: SessionToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        if(Util.arePermissionsGranted(this))
            onStartLogic()
        else
            Util.permissions(this, this)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun onStartLogic() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                Util.REQ
            )
        }
        MyPlayer.initPlayer(this)
        setContentView(R.layout.activity_main)
        bottom = BottomLayout(findViewById(R.id.main))
        MyPlayer.setActiveSongUI(bottom)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        recycle = findViewById(R.id.recycle1)
        recycle.setLayoutManager(LinearLayoutManager(this))
        val serviceIntent = Intent(this, MusicPlayerService::class.java)
        recycle.setAdapter(ItemRecycleAdapter(this, MyPlayer.songs) { _, i ->
            startForegroundService(serviceIntent)
//            controllerFuture.get().play()
            MyPlayer.initQueue(i)
            MyPlayer.startSong(this, MyPlayer.PlayType.CURRENT)

        })
        bottom.bottom.setOnClickListener {
            val intent = Intent(this, ActivityPlayingSong::class.java)
            val animations = ActivityOptions.makeSceneTransitionAnimation(
                this,
                android.util.Pair(bottom.albumArt, "album_cover_transition"),
                android.util.Pair(bottom.artist, "artist_transition")
            )
            startActivity(intent, animations.toBundle())
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart")
        bottom.refresh()
        MyPlayer.setActiveSongUI(bottom)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        MyPlayer.destroy()
        stopService(Intent(this, MusicPlayerService::class.java))

    }

    override fun onStart() {
        super.onStart()
        sessionToken = SessionToken(this, ComponentName(this, MusicPlayerService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Util.REQ && grantResults.isNotEmpty() && grantResults.all {it == PackageManager.PERMISSION_GRANTED}) {
            onStartLogic()
        }
        else {
            Toast.makeText(this, "Permissions are required to proceed", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}