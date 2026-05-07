package com.example.panchify.vistas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.modelos.ComentarioResponse

class ComentariosAdapter(private val lista: List<ComentarioResponse>) :
    RecyclerView.Adapter<ComentariosAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgUserAvatar: ImageView = view.findViewById(R.id.imgUserAvatar)
        val txtUserName: TextView = view.findViewById(R.id.txtUserName)
        val txtCommentText: TextView = view.findViewById(R.id.txtCommentText)
        val txtCommentTime: TextView = view.findViewById(R.id.txtCommentTime)
        val txtLikeCount: TextView = view.findViewById(R.id.txtLikeCount)
        val txtReplyCount: TextView = view.findViewById(R.id.txtReplyCount)
        val txtFireCount: TextView = view.findViewById(R.id.txtFireCount)
        val layoutSongRef: View = view.findViewById(R.id.layoutSongRef)
        val txtSongRef: TextView = view.findViewById(R.id.txtSongRef)
        val txtMusicEmoji: TextView = view.findViewById(R.id.txtMusicEmoji)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comentario = lista[position]

        holder.txtUserName.text = comentario.nombreUsuario ?: "Usuario"
        holder.txtCommentText.text = comentario.texto
        holder.txtCommentTime.text = formatearFecha(comentario.fecha)

        // Puntuación como emoji de estrellas
        val puntuacion = comentario.puntuacion
        if (puntuacion != null && puntuacion > 0) {
            holder.txtMusicEmoji.text = "⭐".repeat(puntuacion.coerceAtMost(5))
        } else {
            holder.txtMusicEmoji.text = "🎵"
        }

        // Contadores (por ahora estáticos, se pueden ampliar con tabla de reacciones)
        holder.txtLikeCount.text = "0"
        holder.txtReplyCount.text = "0"
        holder.txtFireCount.text = "0"

        // Referencia a canción
        val tituloCancion = comentario.tituloCancion
        if (!tituloCancion.isNullOrEmpty() && tituloCancion != "General") {
            holder.layoutSongRef.visibility = View.VISIBLE
            holder.txtSongRef.text = tituloCancion
        } else {
            holder.layoutSongRef.visibility = View.GONE
        }

        // Avatar del usuario
        val imagenUrl = comentario.imagenPerfil
        if (!imagenUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imagenUrl)
                .circleCrop()
                .into(holder.imgUserAvatar)
        }
    }

    override fun getItemCount(): Int = lista.size

    /**
     * Convierte "2026-05-07 13:30:00" a un formato relativo simple
     */
    private fun formatearFecha(fecha: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val date = sdf.parse(fecha) ?: return fecha
            val diff = System.currentTimeMillis() - date.time
            val minutos = diff / 60_000
            val horas = minutos / 60
            val dias = horas / 24

            when {
                minutos < 1 -> "Ahora"
                minutos < 60 -> "Hace ${minutos}min"
                horas < 24 -> "Hace ${horas}h"
                dias < 7 -> "Hace ${dias}d"
                else -> fecha.substring(0, 10) // YYYY-MM-DD
            }
        } catch (e: Exception) {
            fecha
        }
    }
}
