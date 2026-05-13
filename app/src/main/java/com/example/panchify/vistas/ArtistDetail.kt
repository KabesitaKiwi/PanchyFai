package com.example.panchify.vistas

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.api.RetrofitClient
import com.example.panchify.db.ArtistaDao
import com.example.panchify.modelos.ArtistsResponse
import com.example.panchify.preferences.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtistDetail : AppCompatActivity() {

    companion object {
        const val EXTRA_ARTIST_ID = "artist_id"
        const val EXTRA_ARTIST_NAME = "artist_name"
    }

    private lateinit var imgArtist: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtGenre: TextView
    private lateinit var txtPopularity: TextView
    private lateinit var txtAppStats: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_detail)

        imgArtist = findViewById(R.id.imgArtistDetail)
        txtName = findViewById(R.id.txtArtistDetailName)
        txtGenre = findViewById(R.id.txtArtistDetailGenre)
        txtPopularity = findViewById(R.id.txtArtistDetailPopularity)
        txtAppStats = findViewById(R.id.txtArtistDetailAppStats)
        progressBar = findViewById(R.id.progressArtistDetail)

        findViewById<ImageButton>(R.id.btnBackArtistDetail).setOnClickListener {
            finish()
        }

        txtName.text = intent.getStringExtra(EXTRA_ARTIST_NAME) ?: "Artista"
        cargarArtista()
    }

    private fun cargarArtista() {
        val artistId = intent.getStringExtra(EXTRA_ARTIST_ID)
        val token = SessionManager(this).getAccessToken()

        if (artistId.isNullOrBlank() || token == null) {
            Toast.makeText(this, "No se pudo cargar el artista", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        RetrofitClient.spotifyApiService.getArtists("Bearer $token", artistId)
            .enqueue(object : Callback<ArtistsResponse> {
                override fun onResponse(
                    call: Call<ArtistsResponse>,
                    response: Response<ArtistsResponse>
                ) {
                    progressBar.visibility = View.GONE
                    val artist = response.body()?.artists?.firstOrNull()
                    if (!response.isSuccessful || artist == null) {
                        Toast.makeText(this@ArtistDetail, "No se pudo cargar el artista", Toast.LENGTH_SHORT).show()
                        return
                    }

                    txtName.text = artist.name
                    txtGenre.text = artist.genres.firstOrNull() ?: "Sin genero principal"
                    txtPopularity.text = "Popularidad: ${artist.popularity}/100"

                    artist.images.firstOrNull()?.url?.let { imageUrl ->
                        Glide.with(this@ArtistDetail)
                            .load(imageUrl)
                            .circleCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(imgArtist)
                    }

                    Thread {
                        ArtistaDao.guardarArtistas(listOf(artist))
                        val idUsuario = SessionManager(this@ArtistDetail).getUserId()
                        if (idUsuario != null) {
                            val stats = ArtistaDao.obtenerEstadisticasArtistaUsuario(artist.id, idUsuario)
                            runOnUiThread {
                                txtAppStats.text = "En Panchify: ${stats.cancionesEnTusPlaylists} canciones en ${stats.playlistsConEsteArtista} playlists tuyas"
                            }
                        }
                    }.start()
                }

                override fun onFailure(call: Call<ArtistsResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ArtistDetail, "Error cargando artista", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
