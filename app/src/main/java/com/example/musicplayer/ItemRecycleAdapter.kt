package com.example.musicplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemRecycleAdapter(private val context: Context, private val songs: MutableList<MusicItem>, private val listener: (MusicItem, Int) -> Unit) : RecyclerView.Adapter<ItemRecycleAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View =LayoutInflater.from(parent.context).inflate(R.layout.recycleitem, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun getItemCount() = songs.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val cur = songs[position]
        holder.update(cur)
        holder.itemView.setOnClickListener {
            listener(cur, position)
        }
    }


    class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.recycletitle)
        private val artist: TextView = itemView.findViewById(R.id.recycleempty)
        private var path = ""
        private var image: ImageView = itemView.findViewById(R.id.albumArt)
        fun update(song: MusicItem) {
            this.title.text = song.name
            this.artist.text = song.artist
            this.path = song.path
            Util.getImageView(song.art, context, image)
        }
    }
}