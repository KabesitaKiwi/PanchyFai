package com.example.panchify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.modelos.FriendItem

class UserSearchAdapter(
    private var users: List<FriendItem>,
    private val onUserClick: (FriendItem) -> Unit
) : RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgFriendAvatar)
        val txtName: TextView = view.findViewById(R.id.txtFriendName)
        val txtStatus: TextView = view.findViewById(R.id.txtFriendStatus)
        val actions: View = view.findViewById(R.id.layoutFriendActions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.txtName.text = user.nombreUsuario ?: user.spotifyId ?: "Usuario"
        holder.txtStatus.text = when (user.estado) {
            "aceptada" -> "Ya es tu amigo"
            "pendiente" -> "Solicitud pendiente"
            "rechazada" -> "Solicitud rechazada, puedes reenviarla"
            else -> user.email ?: user.spotifyId ?: "Toca para invitar"
        }
        holder.actions.visibility = View.GONE
        holder.itemView.setOnClickListener { onUserClick(user) }

        if (!user.imagenPerfil.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(user.imagenPerfil)
                .circleCrop()
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(holder.imgAvatar)
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }

    override fun getItemCount(): Int = users.size

    fun submitList(newUsers: List<FriendItem>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
