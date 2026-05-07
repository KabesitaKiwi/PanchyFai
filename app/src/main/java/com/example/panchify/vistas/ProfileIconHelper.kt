package com.example.panchify.vistas

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.UserProfileResponse
import com.example.panchify.preferences.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Carga la foto de perfil de Spotify en el ImageView con id [imgProfileIconId]
 * y al hacer click navega al perfil.
 */
fun Activity.cargarIconoPerfil(imgProfileIconId: Int = R.id.imgProfileIcon) {
    val token = SessionManager(this).getAccessToken() ?: return
    val imgView = findViewById<ImageView>(imgProfileIconId) ?: return

    imgView.setOnClickListener {
        startActivity(Intent(this, Profile::class.java))
    }

    RetrofitClient.spotifyApiService.getProfile("Bearer $token")
        .enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val imgUrl = response.body()!!.images?.firstOrNull()?.url
                    if (imgUrl != null) {
                        Glide.with(this@cargarIconoPerfil)
                            .load(imgUrl)
                            .circleCrop()
                            .into(imgView)
                    }
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                // Si falla, dejamos el icono por defecto
            }
        })
}
