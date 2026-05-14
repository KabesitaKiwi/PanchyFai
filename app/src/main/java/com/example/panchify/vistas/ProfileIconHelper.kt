package com.example.panchify.vistas

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.UserProfileResponse
import com.example.panchify.preferences.SessionManager
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Carga la foto de perfil de Spotify en el ImageView con id [imgProfileIconId]
 * y al hacer click abre el menu lateral en la pantalla actual.
 */
fun Activity.cargarIconoPerfil(imgProfileIconId: Int = R.id.imgProfileIcon) {
    val token = SessionManager(this).getAccessToken() ?: return
    val imgView = findViewById<ImageView>(imgProfileIconId) ?: return

    imgView.setOnClickListener {
        mostrarMenuLateralPerfil(token)
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

private fun Activity.mostrarMenuLateralPerfil(token: String) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val navigationView = NavigationView(this).apply {
        setBackgroundColor(resources.getColor(R.color.fondo_principal, theme))
        itemIconTintList = resources.getColorStateList(R.color.spotify_green, theme)
        itemTextColor = resources.getColorStateList(R.color.white, theme)
        inflateHeaderView(R.layout.nav_header)
        inflateMenu(R.menu.drawer_menu)
    }

    dialog.setContentView(navigationView)
    dialog.window?.apply {
        setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        setLayout((resources.displayMetrics.widthPixels * 0.82f).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        setGravity(Gravity.START)
    }

    navigationView.setNavigationItemSelectedListener { menuItem ->
        dialog.dismiss()
        when (menuItem.itemId) {
            R.id.nav_profile -> startActivity(Intent(this, Profile::class.java))
            R.id.nav_playlists -> startActivity(Intent(this, Playlists::class.java))
            R.id.nav_privacy -> Toast.makeText(this, "Politica de Privacidad", Toast.LENGTH_SHORT).show()
            R.id.nav_logout -> {
                SessionManager(this).clearSession()
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.spotify.com/logout"))
                startActivity(browserIntent)

                val loginIntent = Intent(this, Login::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(loginIntent)
                finish()
            }
        }
        true
    }

    cargarCabeceraMenuLateral(navigationView, token)
    dialog.show()
    dialog.window?.apply {
        setLayout((resources.displayMetrics.widthPixels * 0.82f).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        setGravity(Gravity.START)
    }
}

private fun Activity.cargarCabeceraMenuLateral(navigationView: NavigationView, token: String) {
    val header = navigationView.getHeaderView(0)
    val imgHeader = header.findViewById<ImageView>(R.id.imgNavHeaderProfile)
    val txtName = header.findViewById<TextView>(R.id.txtNavHeaderName)

    RetrofitClient.spotifyApiService.getProfile("Bearer $token")
        .enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                if (!response.isSuccessful || response.body() == null) return
                val profile = response.body()!!
                val prefs = getSharedPreferences("panchify_profile", android.content.Context.MODE_PRIVATE)
                txtName.text = prefs.getString("custom_name", profile.display_name ?: "Usuario")
                profile.images?.firstOrNull()?.url?.let { imgUrl ->
                    Glide.with(this@cargarCabeceraMenuLateral)
                        .load(imgUrl)
                        .circleCrop()
                        .into(imgHeader)
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {}
        })
}
