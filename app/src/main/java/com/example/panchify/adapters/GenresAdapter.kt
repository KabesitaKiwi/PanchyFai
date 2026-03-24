package com.example.panchify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R

// Each item: genre name + count
data class GenreCount(val genre: String, val count: Int)

class GenresAdapter(private val genres: List<GenreCount>) :
    RecyclerView.Adapter<GenresAdapter.GenreViewHolder>() {

    class GenreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAlbum: ImageView = view.findViewById(R.id.imgAlbum)
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtArtistName: TextView = view.findViewById(R.id.txtArtistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val item = genres[position]
        holder.txtSongName.text = item.genre
        holder.txtArtistName.text = "${item.count} artista${if (item.count > 1) "s" else ""}"
        // No image for genres – hide or leave placeholder
        holder.imgAlbum.setImageResource(android.R.drawable.ic_menu_gallery)
    }

    override fun getItemCount(): Int = genres.size
}
