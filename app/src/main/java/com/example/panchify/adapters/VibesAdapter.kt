package com.example.panchify.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.modelos.VibeCard
import com.google.android.material.card.MaterialCardView

class VibesAdapter : RecyclerView.Adapter<VibesAdapter.VibeViewHolder>() {

    private val vibes = mutableListOf<VibeCard>()

    fun submitList(nuevasVibes: List<VibeCard>) {
        vibes.clear()
        vibes.addAll(nuevasVibes)
        notifyDataSetChanged()
    }

    class VibeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardVibe)
        val icon: ImageView = view.findViewById(R.id.imgVibeIcon)
        val exampleImage: ImageView = view.findViewById(R.id.imgVibeExample)
        val percentage: TextView = view.findViewById(R.id.txtVibePercentage)
        val name: TextView = view.findViewById(R.id.txtVibeName)
        val description: TextView = view.findViewById(R.id.txtVibeDescription)
        val exampleSong: TextView = view.findViewById(R.id.txtVibeExampleSong)
        val exampleArtist: TextView = view.findViewById(R.id.txtVibeExampleArtist)
        val progress: ProgressBar = view.findViewById(R.id.progressVibe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VibeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vibe_card, parent, false)
        return VibeViewHolder(view)
    }

    override fun onBindViewHolder(holder: VibeViewHolder, position: Int) {
        val vibe = vibes[position]
        val color = Color.parseColor(vibe.color)

        holder.card.setCardBackgroundColor(color)
        holder.icon.setImageResource(iconFor(vibe.id))
        holder.icon.setColorFilter(Color.rgb(17, 17, 17))
        holder.percentage.text = "${vibe.porcentaje}%"
        holder.name.text = vibe.nombre
        holder.description.text = vibe.descripcion
        holder.exampleSong.text = "Ej: ${vibe.ejemploCancion}"
        holder.exampleArtist.text = vibe.ejemploArtista
        holder.progress.progress = vibe.porcentaje
        holder.progress.progressDrawable?.setTint(Color.rgb(30, 60, 45))

        if (vibe.imagenUrl != null) {
            holder.exampleImage.clearColorFilter()
            Glide.with(holder.itemView.context)
                .load(vibe.imagenUrl)
                .placeholder(iconFor(vibe.id))
                .into(holder.exampleImage)
        } else {
            holder.exampleImage.setImageResource(iconFor(vibe.id))
            holder.exampleImage.setColorFilter(Color.rgb(17, 17, 17))
        }

        holder.icon.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.argb(70, 255, 255, 255))
        }
    }

    override fun getItemCount(): Int = vibes.size

    private fun iconFor(id: String): Int {
        return when (id) {
            "energeticas", "intensas", "epicas", "electronicas" -> R.drawable.ic_vibe_bolt
            "sad", "oscuras", "nocturnas", "lentas" -> R.drawable.ic_vibe_moon
            "positivas", "alegres", "suaves" -> R.drawable.ic_vibe_heart
            "acusticas", "organicas", "relajadas", "calmadas" -> R.drawable.ic_vibe_leaf
            "habladas" -> R.drawable.ic_vibe_mic
            "rapidas" -> R.drawable.ic_vibe_speed
            "resumen" -> R.drawable.ic_vibe_star
            else -> R.drawable.ic_vibe_music
        }
    }
}
