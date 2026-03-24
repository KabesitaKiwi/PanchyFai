package com.example.panchify.vistas

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R
import com.example.panchify.adapters.GenreCount
import com.example.panchify.adapters.GenresAdapter
import com.example.panchify.adapters.TopAlbumsAdapter
import com.example.panchify.adapters.TopArtistsAdapter
import com.example.panchify.adapters.TopTracksAdapter
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.TopArtistsResponse
import com.example.panchify.modelos.TopTracksResponse
import com.example.panchify.preferences.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Songs : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var listaCanciones: RecyclerView
    private lateinit var progressBar: ProgressBar

    // Estado actual
    private var tabSeleccionado = 0          // 0=Canciones, 1=Artistas, 2=Álbumes, 3=Género
    private var periodoSeleccionado = "short_term"  // short_term / medium_term / long_term

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)

        sessionManager = SessionManager(this)

        listaCanciones = findViewById(R.id.listaCanciones)
        listaCanciones.layoutManager = LinearLayoutManager(this)
        progressBar = findViewById(R.id.progressCanciones)

        // ── Bottom Navigation ──────────────────────────────────────────────────
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_songs
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val targetClass = when (menuItem.itemId) {
                R.id.nav_home     -> if (this !is Home)     Home::class.java     else null
                R.id.nav_songs    -> if (this !is Songs)    Songs::class.java    else null
                R.id.nav_stats    -> if (this !is Stats)    Stats::class.java    else null
                R.id.nav_comments -> if (this !is Comments) Comments::class.java else null
                R.id.nav_friends  -> if (this !is Friends)  Friends::class.java  else null
                else -> null
            }
            if (targetClass != null) {
                val intent = android.content.Intent(this, targetClass)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                               android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            true
        }

        // ── Tabs (Canciones / Artistas / Álbumes / Género) ────────────────────
        val tabLayout = findViewById<TabLayout>(R.id.seleccionadorCanciones)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabSeleccionado = tab.position
                cargarDatos()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // ── Selector de tiempo (4 semanas / 6 meses / 1 año) ─────────────────
        val timeSelector = findViewById<MaterialButtonToggleGroup>(R.id.timeSelector)
        timeSelector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                periodoSeleccionado = when (checkedId) {
                    R.id.boton4SemanasCanciones -> "short_term"
                    R.id.boton6mesesCanciones   -> "medium_term"
                    R.id.boton1anoCanciones     -> "long_term"
                    else -> "short_term"
                }
                cargarDatos()
            }
        }

        // Carga inicial
        cargarDatos()
    }

    private fun cargarDatos() {
        when (tabSeleccionado) {
            0 -> cargarCanciones()       // Canciones
            1 -> cargarArtistas()        // Artistas
            2 -> cargarAlbumes()         // Álbumes (derivado de top tracks)
            3 -> cargarGeneros()         // Género   (derivado de top artistas)
        }
    }

    // ── Canciones ─────────────────────────────────────────────────────────────
    private fun cargarCanciones() {
        val token = sessionManager.getAccessToken() ?: return
        mostrarCarga(true)

        RetrofitClient.spotifyApiService.getTopTracks(
            authHeader = "Bearer $token",
            timeRange  = periodoSeleccionado
        ).enqueue(object : Callback<TopTracksResponse> {
            override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                mostrarCarga(false)
                if (response.isSuccessful && response.body() != null) {
                    listaCanciones.adapter = TopTracksAdapter(response.body()!!.items)
                }
            }
            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                mostrarCarga(false)
            }
        })
    }

    // ── Artistas ──────────────────────────────────────────────────────────────
    private fun cargarArtistas() {
        val token = sessionManager.getAccessToken() ?: return
        mostrarCarga(true)

        RetrofitClient.spotifyApiService.getTopArtists(
            authHeader = "Bearer $token",
            timeRange  = periodoSeleccionado
        ).enqueue(object : Callback<TopArtistsResponse> {
            override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                mostrarCarga(false)
                if (response.isSuccessful && response.body() != null) {
                    listaCanciones.adapter = TopArtistsAdapter(response.body()!!.items)
                }
            }
            override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                mostrarCarga(false)
            }
        })
    }

    // ── Álbumes (extraídos de top tracks) ────────────────────────────────────
    private fun cargarAlbumes() {
        val token = sessionManager.getAccessToken() ?: return
        mostrarCarga(true)

        RetrofitClient.spotifyApiService.getTopTracks(
            authHeader = "Bearer $token",
            timeRange  = periodoSeleccionado,
            limit      = 50   // Pedimos más para tener variedad de álbumes
        ).enqueue(object : Callback<TopTracksResponse> {
            override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                mostrarCarga(false)
                if (response.isSuccessful && response.body() != null) {
                    listaCanciones.adapter = TopAlbumsAdapter(response.body()!!.items)
                }
            }
            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                mostrarCarga(false)
            }
        })
    }

    // ── Géneros (agregados desde top artistas) ────────────────────────────────
    private fun cargarGeneros() {
        val token = sessionManager.getAccessToken() ?: return
        mostrarCarga(true)

        RetrofitClient.spotifyApiService.getTopArtists(
            authHeader = "Bearer $token",
            timeRange  = periodoSeleccionado,
            limit      = 50
        ).enqueue(object : Callback<TopArtistsResponse> {
            override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                mostrarCarga(false)
                if (response.isSuccessful && response.body() != null) {
                    val genreMap = mutableMapOf<String, Int>()
                    response.body()!!.items.forEach { artist ->
                        artist.genres.forEach { genre ->
                            genreMap[genre] = (genreMap[genre] ?: 0) + 1
                        }
                    }
                    val sortedGenres = genreMap.entries
                        .sortedByDescending { it.value }
                        .map { GenreCount(it.key, it.value) }
                    listaCanciones.adapter = GenresAdapter(sortedGenres)
                }
            }
            override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                mostrarCarga(false)
            }
        })
    }

    private fun mostrarCarga(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_songs)?.isChecked = true
    }
}
