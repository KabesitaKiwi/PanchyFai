package com.example.panchify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.modelos.SpotifyPlaylistSimple

class PlaylistsAdapter(
    private val playlists: List<SpotifyPlaylistSimple>,
    private val onPlaylistClick: (SpotifyPlaylistSimple) -> Unit
) : RecyclerView.Adapter<PlaylistsAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPlaylist: ImageView = view.findViewById(R.id.imgPlaylist)
        val txtName: TextView = view.findViewById(R.id.txtPlaylistName)
        val txtDescription: TextView = view.findViewById(R.id.txtPlaylistDescription)
        val txtCount: TextView = view.findViewById(R.id.txtPlaylistCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.txtName.text = playlist.name
        holder.txtDescription.text = playlist.description?.takeIf { it.isNotBlank() } ?: "Sin descripcion"
        holder.txtCount.text = "${playlist.tracks.total} canciones"
        holder.imgPlaylist.clearColorFilter()
        holder.imgPlaylist.setPadding(0, 0, 0, 0)

        val imageUrl = playlist.images.firstOrNull()?.url
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.imgPlaylist)
        } else {
            holder.imgPlaylist.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener { onPlaylistClick(playlist) }
    }

    override fun getItemCount(): Int = playlists.size
}
