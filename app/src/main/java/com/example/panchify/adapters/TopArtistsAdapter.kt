package com.example.panchify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.modelos.ArtistFull

class TopArtistsAdapter(private val artists: List<ArtistFull>) :
    RecyclerView.Adapter<TopArtistsAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAlbum: ImageView = view.findViewById(R.id.imgAlbum)
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtArtistName: TextView = view.findViewById(R.id.txtArtistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.txtSongName.text = artist.name
        holder.txtArtistName.text = artist.genres.take(2).joinToString(", ").ifEmpty { "—" }

        if (artist.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(artist.images[0].url)
                .into(holder.imgAlbum)
        }
    }

    override fun getItemCount(): Int = artists.size
}
