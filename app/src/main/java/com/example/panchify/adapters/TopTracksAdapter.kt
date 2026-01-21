package com.example.panchify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.modelos.Track

class TopTracksAdapter(private val tracks: List<Track>) :
    RecyclerView.Adapter<TopTracksAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAlbum: ImageView = view.findViewById(R.id.imgAlbum)
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtArtistName: TextView = view.findViewById(R.id.txtArtistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]

        holder.txtSongName.text = track.name
        holder.txtArtistName.text = track.artists.joinToString(", ") { it.name }

        // Cargar imagen con Glide si existe
        if (track.album.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(track.album.images[0].url)
                .into(holder.imgAlbum)
        }
    }

    override fun getItemCount(): Int = tracks.size
}
