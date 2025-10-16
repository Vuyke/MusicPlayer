package com.example.musicplayer.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R

class ItemRecycleAdapter(private val context: Context, private val songs: SongArray, private val click: (MediaItem, Int) -> Unit, private val longClick: (MediaItem, Int) -> Unit) : RecyclerView.Adapter<ItemRecycleAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycle_item, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun getItemCount() = songs.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val cur = songs.get(position)
        holder.update(cur)
        holder.itemView.setOnClickListener {
            click(cur, position)
        }
        holder.itemView.setOnLongClickListener {
            longClick(cur, position)
            true
        }
    }

    class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.recycleTitle)
        private val artist: TextView = itemView.findViewById(R.id.recycleArtist)
        private var path = ""
        private var image: ImageView = itemView.findViewById(R.id.albumCover)
        fun update(song: MediaItem) {
            this.title.text = song.mediaMetadata.title
            this.artist.text = Util.artistAlbumFormat(song.mediaMetadata.artist, song.mediaMetadata.albumTitle)
            this.path = song.mediaId
            Util.getImageView(song.mediaMetadata.artworkUri, context, image, R.drawable.launch_icon)
        }
    }
}