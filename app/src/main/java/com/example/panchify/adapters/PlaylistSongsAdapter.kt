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
import com.example.panchify.modelos.CancionBD
import com.example.panchify.preferences.SessionManager

class PlaylistSongsAdapter(
    private val canciones: List<CancionBD>,
    private val spotifyPlaylistId: String
) :
    RecyclerView.Adapter<PlaylistSongsAdapter.CancionViewHolder>() {

    class CancionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAlbum: ImageView = view.findViewById(R.id.imgAlbum)
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtArtistName: TextView = view.findViewById(R.id.txtArtistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return CancionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
        val cancion = canciones[position]
        holder.txtSongName.text = cancion.titulo
        holder.txtArtistName.text = listOfNotNull(cancion.artista, cancion.album)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
            .ifBlank { "Playlist" }

        if (!cancion.imagenUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(cancion.imagenUrl)
                .into(holder.imgAlbum)
        } else {
            holder.imgAlbum.setImageResource(android.R.drawable.ic_media_play)
        }

        holder.imgAlbum.setOnClickListener {
            val token = SessionManager(holder.itemView.context).getAccessToken()
            if (token == null) {
                Toast.makeText(holder.itemView.context, "No se encontro la sesion de Spotify", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AudioPlayer.playPlaylistTrack(
                token = token,
                playlistId = spotifyPlaylistId,
                trackId = cancion.idCancion,
                onStart = {
                    Toast.makeText(holder.itemView.context, "Reproduciendo desde playlist...", Toast.LENGTH_SHORT).show()
                },
                onStop = {},
                onError = {
                    Toast.makeText(holder.itemView.context, "No se pudo reproducir la cancion", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun getItemCount(): Int = canciones.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        AudioPlayer.release()
    }
}
