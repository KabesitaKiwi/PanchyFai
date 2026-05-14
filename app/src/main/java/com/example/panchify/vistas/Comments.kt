package com.example.panchify.vistas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.panchify.R
import com.example.panchify.adapters.SearchTracksAdapter
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.SearchResponse
import com.example.panchify.modelos.Track
import com.example.panchify.modelos.ComentarioResponse
import com.example.panchify.preferences.SessionManager
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Comments : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ComentariosAdapter
    private lateinit var campoComentario: EditText
    private lateinit var btnSend: MaterialButton

    private lateinit var searchView: SearchView
    private lateinit var layoutCancionSeleccionada: View
    private lateinit var imgSelectedTrack: ImageView
    private lateinit var txtSelectedTrackName: TextView
    private lateinit var txtSelectedArtistName: TextView
    private lateinit var btnCambiarCancion: ImageView

    private var selectedTrackId: String? = null
    private var selectedTrackName: String? = null
    private var selectedTrackImage: String? = null
    private var selectedTrackPreview: String? = null
    private var selectedTrackAlbum: String? = null
    private var selectedTrackDurationMs: Long? = null
    private var selectedTrackYear: String? = null
    private var selectedTrackSpotifyUrl: String? = null
    private var globalComments: List<ComentarioResponse> = emptyList()
    
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val targetClass = when (menuItem.itemId) {
                R.id.nav_home -> if (this !is Home) Home::class.java else null
                R.id.nav_songs -> if (this !is Songs) Songs::class.java else null
                R.id.nav_stats -> if (this !is Stats) Stats::class.java else null
                R.id.nav_comments -> if (this !is Comments) Comments::class.java else null
                R.id.nav_friends -> if (this !is Friends) Friends::class.java else null
                else -> null
            }
            if (targetClass != null) {
                val targetIntent = android.content.Intent(this, targetClass)
                targetIntent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(targetIntent)
                overridePendingTransition(0, 0)
            }
            true
        }

        recycler = findViewById(R.id.listaComentarios)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ComentariosAdapter(emptyList())
        recycler.adapter = adapter

        campoComentario = findViewById(R.id.campoComentarios)
        btnSend = findViewById(R.id.btnSend)
        
        searchView = findViewById(R.id.searchViewCanciones)
        layoutCancionSeleccionada = findViewById(R.id.layoutCancionSeleccionada)
        imgSelectedTrack = findViewById(R.id.imgSelectedTrack)
        txtSelectedTrackName = findViewById(R.id.txtSelectedTrackName)
        txtSelectedArtistName = findViewById(R.id.txtSelectedArtistName)
        btnCambiarCancion = findViewById(R.id.btnCambiarCancion)

        btnSend.setOnClickListener {
            enviarComentario()
        }

        btnCambiarCancion.setOnClickListener {
            clearSelectedTrack()
        }

        setupSearchView()
        aplicarColoresBuscador()

        this.cargarIconoPerfil()
        updateInputState()
        
        // Cargar todos los comentarios al inicio
        cargarComentarios()
    }

    private fun updateInputState() {
        val hasTrack = selectedTrackId != null
        campoComentario.isEnabled = hasTrack
        btnSend.isEnabled = hasTrack
        if (!hasTrack) {
            campoComentario.hint = "Selecciona una canción primero..."
        } else {
            campoComentario.hint = "Escribe un comentario..."
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchTracks(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }

                if (newText.isNullOrEmpty()) {
                    // Restaurar feed global
                    adapter = ComentariosAdapter(globalComments)
                    recycler.adapter = adapter
                } else if (newText.length >= 2) {
                    searchRunnable = Runnable {
                        searchTracks(newText)
                    }
                    searchHandler.postDelayed(searchRunnable!!, 500)
                }
                return true
            }
        })
    }

    private fun aplicarColoresBuscador() {
        val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText?.setTextColor(android.graphics.Color.BLACK)
        searchText?.setHintTextColor(android.graphics.Color.BLACK)
        searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
            ?.setColorFilter(android.graphics.Color.BLACK)
        searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            ?.setColorFilter(android.graphics.Color.BLACK)
    }

    private fun searchTracks(query: String) {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getAccessToken() ?: return

        RetrofitClient.spotifyApiService.searchTracks(
            authHeader = "Bearer $token",
            query = query
        ).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val tracks = response.body()!!.tracks.items
                    val searchAdapter = SearchTracksAdapter(tracks) { track ->
                        onTrackSelected(track)
                    }
                    recycler.adapter = searchAdapter
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Toast.makeText(this@Comments, "Error buscando canciones", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onTrackSelected(track: Track) {
        selectedTrackId = track.id
        selectedTrackName = track.name
        selectedTrackImage = track.album.images.firstOrNull()?.url
        selectedTrackPreview = track.preview_url
        selectedTrackAlbum = track.album.name
        selectedTrackDurationMs = track.duration_ms
        selectedTrackYear = track.album.release_date?.take(4) // Extract year YYYY
        selectedTrackSpotifyUrl = track.external_urls?.spotify
        
        layoutCancionSeleccionada.visibility = View.VISIBLE
        searchView.visibility = View.GONE
        
        txtSelectedTrackName.text = track.name
        txtSelectedArtistName.text = track.artists.joinToString(", ") { it.name }
        
        if (track.album.images.isNotEmpty()) {
            Glide.with(this)
                .load(track.album.images[0].url)
                .into(imgSelectedTrack)
        }
        
        updateInputState()
        searchView.setQuery("", false)
        
        // Restaurar feed global de inmediato
        adapter = ComentariosAdapter(globalComments)
        recycler.adapter = adapter
    }

    private fun clearSelectedTrack() {
        selectedTrackId = null
        selectedTrackName = null
        selectedTrackImage = null
        selectedTrackPreview = null
        selectedTrackAlbum = null
        selectedTrackDurationMs = null
        selectedTrackYear = null
        selectedTrackSpotifyUrl = null
        
        layoutCancionSeleccionada.visibility = View.GONE
        searchView.visibility = View.VISIBLE
        
        updateInputState()
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_comments)?.isChecked = true
    }

    private fun cargarComentarios() {
        Thread {
            val comentarios = com.example.panchify.db.ComentarioDao.listarComentarios()
            
            runOnUiThread {
                globalComments = comentarios
                if (searchView.query.isNullOrEmpty()) {
                    adapter = ComentariosAdapter(globalComments)
                    recycler.adapter = adapter
                }
            }
        }.start()
    }

    private fun enviarComentario() {
        val texto = campoComentario.text?.toString()?.trim() ?: ""
        if (texto.isEmpty()) {
            Toast.makeText(this, "Escribe algo primero", Toast.LENGTH_SHORT).show()
            return
        }

        val idCancion = selectedTrackId ?: "general"
        val nombreCancion = selectedTrackName ?: "General"

        val sessionManager = SessionManager(this)
        val idUsuario = sessionManager.getUserId()
        if (idUsuario == null) {
            Toast.makeText(this, "Error: usuario no registrado", Toast.LENGTH_SHORT).show()
            return
        }

        btnSend.isEnabled = false

        Thread {
            val resultado = com.example.panchify.db.ComentarioDao.crearComentario(
                idUsuario,
                idCancion,
                texto,
                null,
                nombreCancion,
                selectedTrackImage,
                selectedTrackPreview,
                selectedTrackAlbum,
                selectedTrackDurationMs,
                selectedTrackYear,
                selectedTrackSpotifyUrl
            )
            
            runOnUiThread {
                btnSend.isEnabled = true
                if (resultado != null) {
                    campoComentario.text?.clear()
                    clearSelectedTrack()
                    cargarComentarios()
                    Toast.makeText(this@Comments, "Comentario publicado 🎵", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@Comments, "Error al publicar", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
