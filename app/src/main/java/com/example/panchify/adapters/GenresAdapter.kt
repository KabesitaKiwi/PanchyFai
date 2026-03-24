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

data class GenreCount(
    val genre: String,
    val count: Int,
    val artists: List<ArtistFull> = emptyList()
)

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

        // Título: nombre del género
        holder.txtSongName.text = item.genre.replaceFirstChar { it.uppercase() }

        // Subtítulo: hasta 4 artistas del género
        val artistNames = item.artists.take(4).joinToString(", ") { it.name }
        holder.txtArtistName.text = if (artistNames.isNotEmpty()) artistNames
                                    else "${item.count} artista${if (item.count > 1) "s" else ""}"

        // Imagen: foto del primer artista que tenga imagen
        val imageUrl = item.artists
            .firstOrNull { it.images.isNotEmpty() }
            ?.images?.firstOrNull()?.url

        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgAlbum)
        } else {
            holder.imgAlbum.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun getItemCount(): Int = genres.size
}

