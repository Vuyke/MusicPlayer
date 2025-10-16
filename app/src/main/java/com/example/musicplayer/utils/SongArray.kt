package com.example.musicplayer.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.nio.file.Paths

class SongArray(context: Context? = null) {
    private val indexArray: MutableList<Int> = mutableListOf()
    private val songs: MutableList<MediaItem> = mutableListOf()

    init {
        synchronized(indexArray) {
            context?.let {
                getSongFiles(context)
                for (i in 0..<songs.size) {
                    indexArray.add(i)
                }
            }
        }
    }

    val size: Int
        get() = songs.size

    fun get(ind: Int): MediaItem {
        synchronized(indexArray) {
            return songs[indexArray[ind]]
        }
    }

    fun getIndex(ind: Int): Int {
        synchronized(indexArray) {
            return indexArray[ind]
        }
    }

    fun getMediaItems() = songs

    fun set(ind: Int, mediaItem: MediaItem) {
        synchronized(indexArray) {
            songs[indexArray[ind]] = mediaItem
        }
    }

    fun remove(ind: Int) {
        synchronized(indexArray) {
            val temp = indexArray[ind]
            Log.d("Removal","Removed song on $ind spot, in index array: ${indexArray[ind]}, in songs array: ${songs[temp]}")
            songs.removeAt(temp)
            indexArray.removeAt(ind)
            for (i in 0..<indexArray.size) {
                if (indexArray[i] > temp) indexArray[i]--
            }
        }
    }

    private fun getPath(path: String, newName: String): String {
        val file = Paths.get(path)
        val par = file.parent.toString()
        val dotIndex = path.lastIndexOf('.')
        if(dotIndex == -1)
            return path
        val final = par + "/" + newName + path.substring(dotIndex)
        return final
    }

    fun updateName(ind: Int, newName: String) {
        val prev = get(ind)
        val artist = prev.mediaMetadata.artist.toString()
        val artPath = prev.mediaMetadata.artworkUri ?: Uri.parse("")
        val path = getPath(prev.localConfiguration?.uri.toString(), newName)
        val albumTitle = prev.mediaMetadata.albumTitle.toString()
        val songPath = prev.mediaId
        val extras = prev.mediaMetadata.extras
        set(ind, createMediaItem(newName, artist, path, artPath, albumTitle, songPath, extras))
    }

    private fun getSongFiles(context: Context) {
        songs.clear()
        val contentResolver: ContentResolver = context.contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val collect: Array<String> = arrayOf(
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.RELATIVE_PATH
        )
        val selection =
            "LOWER(${MediaStore.Audio.Media.MIME_TYPE}) IN (?, ?, ?, ?, ?) AND ${MediaStore.Audio.Media.DISPLAY_NAME}<> ?"
        val args = arrayOf(
            "audio/mpeg",
            "audio/mp4",
            "audio/aac",
            "audio/aac-adts",
            "audio/x-wav",
            "tone.mp3"
        )
        val cursor =
            contentResolver.query(uri, collect, selection, args, MediaStore.Audio.Media.DATE_ADDED)
        cursor?.use {
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val idSong = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val idMime = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val relativeId = it.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            while (it.moveToNext()) {
                try {
                    val id = it.getLong(albumIdIndex)
                    val id2 = it.getLong(idSong)
                    val songPath =
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + id2
                    val albumUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart/"),
                        id
                    )
                    val extras = Bundle().apply {
                        putString("mime_type", it.getString(idMime))
                        putString("relative_path", it.getString(relativeId))
                    }
                    songs.add(
                        createMediaItem(
                            it.getString(dataIndex), it.getString(artistIndex),
                            it.getString(pathIndex), albumUri, it.getString(albumIndex),
                            songPath, extras
                        )
                    )
                }
                catch(e: NullPointerException) {
                    Log.d("Loading media", "Loading failed because of null exception.")
                }
            }
        }
        songs.reverse()
    }

    private fun createMediaItem(
        songName: String,
        artist: String,
        path: String,
        artPath: Uri,
        albumTitle: String,
        songPath: String,
        extras: Bundle?
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(songName)
            .setArtist(artist)
            .setArtworkUri(artPath)
            .setExtras(extras)
        if (!artist.contains("unknown", true)) {
            metadata.setAlbumTitle(albumTitle)
        }
        return MediaItem.Builder()
            .setMediaId(songPath)
            .setUri(Uri.parse(path))
            .setMediaMetadata(metadata.build())
            .build()
    }

    fun updateSearch(s: String) {
        synchronized(indexArray) {
            if (s == "") {
                indexArray.sort()
            } else {
                indexArray.sortBy {
                    editDistance(
                        songs[it].mediaMetadata.title.toString().lowercase(), s.lowercase()
                    ).coerceAtMost(
                        editDistance(
                            songs[it].mediaMetadata.artist.toString().lowercase(), s.lowercase()
                        )).coerceAtMost(
                        editDistance(
                            songs[it].mediaMetadata.albumTitle.toString().lowercase(), s.lowercase()
                        ))
                }
            }
        }
    }

    private fun editDistance(text: String, pat: String): Int {
        var dist = 0
        for(p in pat.split(" ")) {
            var best = 1000
            for(s in text.split(" ")) {
                val dp = Array(p.length + 1) {IntArray(s.length + 1)}
                for (j in 0..s.length) {
                    for (i in 0..p.length) {
                        if (j == 0) dp[i][j] = i
                        else if (i == 0) dp[i][j] = j
                        else dp[i][j] = (1 + dp[i - 1][j]).coerceAtMost(1 + dp[i][j - 1])
                            .coerceAtMost(dp[i - 1][j - 1] + (if (s[j - 1] == p[i - 1]) 0 else 1))
                    }
                }
                best = dp[p.length].min().coerceAtMost(best)
            }
            dist += best
        }
        return dist
    }
}
