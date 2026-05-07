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

// Extracts unique albums from tracks list
class TopAlbumsAdapter(tracks: List<Track>) :
    RecyclerView.Adapter<TopAlbumsAdapter.AlbumViewHolder>() {

    // Deduplicate albums by name, keep first occurrence
    private val albums: List<Track> = tracks
        .distinctBy { it.album.name }
        .take(20)

    class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAlbum: ImageView = view.findViewById(R.id.imgAlbum)
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtArtistName: TextView = view.findViewById(R.id.txtArtistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
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
        }

        // Listener para reproducir vista previa de la canción (el primer track del álbum)
        holder.imgAlbum.setOnClickListener {
            AudioPlayer.playOrPause(
                trackId = track.id,
                previewUrl = track.preview_url,
                onStart = {
                    android.widget.Toast.makeText(holder.itemView.context, "Reproduciendo canción...", android.widget.Toast.LENGTH_SHORT).show()
                },
                onStop = {
                    // Opcional
                },
                onError = {
                    android.widget.Toast.makeText(holder.itemView.context, "No se pudo reproducir la canción", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun getItemCount(): Int = albums.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        AudioPlayer.release()
    }
}
