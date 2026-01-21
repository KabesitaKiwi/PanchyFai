package com.example.panchify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R
import com.example.panchify.modelos.Track

class CancionesAdapter(
    private var canciones: List<Track>
) : RecyclerView.Adapter<CancionesAdapter.CancionViewHolder>() {

    class CancionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.txtTitulo)
        val artista: TextView = view.findViewById(R.id.txtArtista)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cancion, parent, false)
        return CancionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
        val cancion = canciones[position]
        holder.titulo.text = cancion.name
        holder.artista.text = cancion.artists.joinToString(", ") { it.name }
    }

    override fun getItemCount(): Int = canciones.size

    fun actualizarDatos(nuevasCanciones: List<Track>) {
        canciones = nuevasCanciones
        notifyDataSetChanged()
    }
}
