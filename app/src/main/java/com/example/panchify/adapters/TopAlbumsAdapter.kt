package com.example.panchify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.modelos.Track
import com.example.panchify.preferences.SessionManager

class TopAlbumsAdapter(tracks: List<Track>) :
    RecyclerView.Adapter<TopAlbumsAdapter.AlbumViewHolder>() {

    private val albums: List<Track> = tracks
        .distinctBy { it.album.id.ifBlank { it.album.name } }
        .take(20)

    class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAlbum: ImageView = view.findViewById(R.id.imgAlbum)
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtArtistName: TextView = view.findViewById(R.id.txtArtistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val track = albums[position]
        holder.txtSongName.text = track.album.name
        holder.txtArtistName.text = track.artists.joinToString(", ") { it.name }

        if (track.album.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(track.album.images[0].url)
                .into(holder.imgAlbum)
        } else {
            holder.imgAlbum.setImageDrawable(null)
        }

        val playAlbumClick = View.OnClickListener {
            val context = holder.itemView.context
            val token = SessionManager(context).getAccessToken()

            if (token == null) {
                Toast.makeText(context, "No se encontro la sesion de Spotify", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (track.album.id.isBlank()) {
                Toast.makeText(context, "No se pudo encontrar el album en Spotify", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            AudioPlayer.playAlbum(
                token = token,
                albumId = track.album.id,
                onStart = {
                    Toast.makeText(context, "Reproduciendo album...", Toast.LENGTH_SHORT).show()
                },
                onStop = {},
                onError = {
                    Toast.makeText(context, "No se pudo reproducir el album", Toast.LENGTH_SHORT).show()
                }
            )
        }

        holder.itemView.setOnClickListener(playAlbumClick)
        holder.imgAlbum.setOnClickListener(playAlbumClick)
    }

    override fun getItemCount(): Int = albums.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        AudioPlayer.release()
    }
}
