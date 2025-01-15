package com.example.musicplayer

import android.Manifest
import android.app.ActivityOptions
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "5"
    private lateinit var recycle: RecyclerView
    private lateinit var bottom: BottomLayout

    private var songs: MutableList<MusicItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createChannel(this)
        Log.d(TAG, "onCreate")
        if(Util.arePermissionsGranted(this))
            onStartLogic()
        else
            Util.permissions(this, this)
    }

    private fun onStartLogic() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), Util.REQ)
        }
        setContentView(R.layout.activity_main)
        MyPlayer.initPlayer()
        bottom = BottomLayout(findViewById(R.id.main))
        MyPlayer.setActiveSongUI(bottom)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        songs = Util.mp3Files(this)
        recycle = findViewById(R.id.recycle1)
        recycle.setLayoutManager(LinearLayoutManager(this))
        recycle.setAdapter(ItemRecycleAdapter(this, songs) { selectedItem, i ->
            MyPlayer.startSong(this, selectedItem)
            MyPlayer.initQueue(songs, i)
            createNotification(this)
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
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
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

    fun createNotification(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.play)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle("Notification TEST")
            .setContentText("Content 1, 2, 3")
            .setAutoCancel(true)
            .build()
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)

    }

    fun createChannel(context: Context) {
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val name = context.getString(R.string.notificationChannel)
        val descriptionText = "Description 1"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        mChannel.setShowBadge(false)
        notificationManager.createNotificationChannel(mChannel)
    }
}