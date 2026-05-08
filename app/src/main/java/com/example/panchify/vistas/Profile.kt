package com.example.panchify.vistas

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.UserProfileResponse
import com.example.panchify.preferences.SessionManager
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val txtSpotifyUsername = findViewById<TextView>(R.id.txtSpotifyUsername)
        val etCustomName = findViewById<TextInputEditText>(R.id.etCustomName)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

        val sessionManager = SessionManager(this)
        val idUsuario = sessionManager.getUserId()

        // Cargar nombre personalizado desde la base de datos MySQL en segundo plano
        if (idUsuario != null) {
            Thread {
                val dbUser = com.example.panchify.db.UsuarioDao.obtenerUsuario(idUsuario)
                runOnUiThread {
                    if (dbUser != null && !dbUser.nombreUsuario.isNullOrEmpty()) {
                        etCustomName.setText(dbUser.nombreUsuario)
                    }
                }
            }.start()
        }

        val token = SessionManager(this).getAccessToken()
        if (token != null) {
            RetrofitClient.spotifyApiService.getProfile("Bearer $token")
                .enqueue(object : Callback<UserProfileResponse> {
                    override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val profile = response.body()!!
                            txtSpotifyUsername.text = profile.display_name ?: "Desconocido"

                            val imageUrl = profile.images?.firstOrNull()?.url
                            if (imageUrl != null) {
                                Glide.with(this@Profile)
                                    .load(imageUrl)
                                    .circleCrop()
                                    .into(imgProfile)
                            }
                        }
                    }

                    override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                        Toast.makeText(this@Profile, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        btnSaveProfile.setOnClickListener {
            val newName = etCustomName.text.toString()
            if (newName.isNotBlank() && idUsuario != null) {
                btnSaveProfile.isEnabled = false
                // Guardar en la base de datos MySQL en segundo plano
                Thread {
                    val exito = com.example.panchify.db.UsuarioDao.actualizarNombreUsuario(idUsuario, newName)
                    runOnUiThread {
                        btnSaveProfile.isEnabled = true
                        if (exito) {
                            Toast.makeText(this@Profile, "Nombre actualizado en la red", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@Profile, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } else {
                Toast.makeText(this, "Escribe un nombre válido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
