package com.example.panchify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.modelos.FriendItem
import com.example.panchify.modelos.FriendItemType

class FriendsAdapter(
    private val items: List<FriendItem>,
    private val onFriendClick: (FriendItem) -> Unit,
    private val onAccept: (FriendItem) -> Unit,
    private val onReject: (FriendItem) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgFriendAvatar)
        val txtName: TextView = view.findViewById(R.id.txtFriendName)
        val txtStatus: TextView = view.findViewById(R.id.txtFriendStatus)
        val layoutActions: LinearLayout = view.findViewById(R.id.layoutFriendActions)
        val btnAccept: ImageButton = view.findViewById(R.id.btnAcceptFriend)
        val btnReject: ImageButton = view.findViewById(R.id.btnRejectFriend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = items[position]
        holder.txtName.text = item.nombreUsuario ?: item.spotifyId ?: "Usuario"

        if (!item.imagenPerfil.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(item.imagenPerfil)
                .circleCrop()
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(holder.imgAvatar)
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        if (item.tipo == FriendItemType.SOLICITUD_RECIBIDA) {
            holder.txtStatus.text = "Quiere ser tu amigo"
            holder.layoutActions.visibility = View.VISIBLE
            holder.itemView.setOnClickListener(null)
            holder.btnAccept.setOnClickListener { onAccept(item) }
            holder.btnReject.setOnClickListener { onReject(item) }
        } else {
            holder.txtStatus.text = item.email ?: "Amigo"
            holder.layoutActions.visibility = View.GONE
            holder.itemView.setOnClickListener { onFriendClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size
}
