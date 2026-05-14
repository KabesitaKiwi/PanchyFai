package com.example.panchify.vistas

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.adapters.TopTracksAdapter
import com.example.panchify.db.TopUsuarioDao

class FriendDetail : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "friend_user_id"
        const val EXTRA_USER_NAME = "friend_user_name"
        const val EXTRA_USER_IMAGE = "friend_user_image"
    }

    private lateinit var recyclerTopTracks: RecyclerView
    private lateinit var txtEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_detail)

        val idUsuario = intent.getIntExtra(EXTRA_USER_ID, -1)
        val nombre = intent.getStringExtra(EXTRA_USER_NAME) ?: "Amigo"
        val imagen = intent.getStringExtra(EXTRA_USER_IMAGE)

        findViewById<ImageButton>(R.id.btnBackFriendDetail).setOnClickListener { finish() }
        findViewById<TextView>(R.id.txtFriendDetailName).text = nombre
        val imgAvatar = findViewById<ImageView>(R.id.imgFriendDetailAvatar)
        if (!imagen.isNullOrBlank()) {
            Glide.with(this)
                .load(imagen)
                .circleCrop()
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(imgAvatar)
        }

        recyclerTopTracks = findViewById(R.id.recyclerFriendTopTracks)
        txtEmpty = findViewById(R.id.txtEmptyFriendTop)
        recyclerTopTracks.layoutManager = LinearLayoutManager(this)

        if (idUsuario == -1) {
            mostrarVacio()
            return
        }

        cargarTopAmigo(idUsuario)
    }

    private fun cargarTopAmigo(idUsuario: Int) {
        Thread {
            val tracks = TopUsuarioDao.obtenerTopCanciones(idUsuario, "short_term", 10)
            runOnUiThread {
                if (tracks.isEmpty()) {
                    mostrarVacio()
                } else {
                    txtEmpty.visibility = View.GONE
                    recyclerTopTracks.adapter = TopTracksAdapter(tracks)
                }
            }
        }.start()
    }

    private fun mostrarVacio() {
        txtEmpty.visibility = View.VISIBLE
        txtEmpty.text = "Todavia no hay datos de este amigo. Se guardaran cuando entre en Panchify."
    }
}
