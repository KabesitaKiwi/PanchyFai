package com.example.panchify.vistas

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R
import com.example.panchify.adapters.VibesAdapter
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.ArtistsResponse
import com.example.panchify.modelos.AudioFeaturesResponse
import com.example.panchify.modelos.TopArtistsResponse
import com.example.panchify.modelos.TopTracksResponse
import com.example.panchify.modelos.Track
import com.example.panchify.modelos.VibeCalculator
import com.example.panchify.modelos.VibeCard
import com.example.panchify.preferences.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Stats : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var txtMinutos: TextView
    private lateinit var txtHoras: TextView
    private lateinit var txtArtistas: TextView
    private lateinit var txtCanciones: TextView

    private lateinit var txtGenero1: TextView
    private lateinit var txtGenero2: TextView
    private lateinit var txtGenero3: TextView
    private lateinit var txtGeneroPorcentaje1: TextView
    private lateinit var txtGeneroPorcentaje2: TextView
    private lateinit var txtGeneroPorcentaje3: TextView
    private lateinit var progressGenero1: ProgressBar
    private lateinit var progressGenero2: ProgressBar
    private lateinit var progressGenero3: ProgressBar
    private lateinit var txtVibes: TextView
    private lateinit var recyclerVibes: RecyclerView
    private lateinit var vibesAdapter: VibesAdapter

    private var periodoSeleccionado = "short_term"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        sessionManager = SessionManager(this)

        txtMinutos = findViewById(R.id.txtStatMinutos)
        txtHoras = findViewById(R.id.txtStatHoras)
        txtArtistas = findViewById(R.id.txtStatArtistas)
        txtCanciones = findViewById(R.id.txtStatCanciones)

        txtGenero1 = findViewById(R.id.txtGenero1)
        txtGenero2 = findViewById(R.id.txtGenero2)
        txtGenero3 = findViewById(R.id.txtGenero3)
        txtGeneroPorcentaje1 = findViewById(R.id.txtGeneroPorcentaje1)
        txtGeneroPorcentaje2 = findViewById(R.id.txtGeneroPorcentaje2)
        txtGeneroPorcentaje3 = findViewById(R.id.txtGeneroPorcentaje3)
        progressGenero1 = findViewById(R.id.progressGenero1)
        progressGenero2 = findViewById(R.id.progressGenero2)
        progressGenero3 = findViewById(R.id.progressGenero3)
        txtVibes = findViewById(R.id.txtVibes)
        recyclerVibes = findViewById(R.id.recyclerVibes)
        vibesAdapter = VibesAdapter()
        recyclerVibes.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            this,
            androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerVibes.adapter = vibesAdapter

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_stats
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val targetClass = when (menuItem.itemId) {
                R.id.nav_home -> Home::class.java
                R.id.nav_songs -> Songs::class.java
                R.id.nav_stats -> null
                R.id.nav_comments -> Comments::class.java
                R.id.nav_friends -> Friends::class.java
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

        val timeSelector = findViewById<MaterialButtonToggleGroup>(R.id.timeSelector)
        timeSelector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                periodoSeleccionado = when (checkedId) {
                    R.id.boton4semanasStats -> "short_term"
                    R.id.boton6mesesStats -> "medium_term"
                    R.id.botonSiempreStats -> "long_term"
                    else -> "short_term"
                }
                cargarEstadisticas()
            }
        }

        cargarEstadisticas()
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_stats)?.isChecked = true
    }

    private fun cargarEstadisticas() {
        val token = sessionManager.getAccessToken() ?: return

        mostrarCargaResumen()
        mostrarCargaGeneros()
        mostrarCargaVibes()
        actualizarTituloVibes()
        cargarResumen(token)
        cargarGeneros(token)
        cargarVibes(token)
    }

    private fun cargarResumen(token: String) {
        RetrofitClient.spotifyApiService.getTopTracks(
            authHeader = "Bearer $token",
            timeRange = periodoSeleccionado,
            limit = 50
        ).enqueue(object : Callback<TopTracksResponse> {
            override fun onResponse(
                call: Call<TopTracksResponse>,
                response: Response<TopTracksResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val tracks = response.body()!!.items
                    val totalMs = tracks.sumOf { it.duration_ms }
                    val minutos = totalMs / 60_000
                    val horas = totalMs / 3_600_000
                    val uniqueArtists = tracks
                        .flatMap { it.artists }
                        .map { it.name }
                        .toSet()

                    txtMinutos.text = "%,d".format(minutos)
                    txtHoras.text = horas.toString()
                    txtArtistas.text = uniqueArtists.size.toString()
                    txtCanciones.text = tracks.distinctBy { it.id }.size.toString()
                } else {
                    mostrarErrorResumen()
                }
            }

            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                mostrarErrorResumen()
            }
        })
    }

    private fun cargarGeneros(token: String) {
        RetrofitClient.spotifyApiService.getTopArtists(
            authHeader = "Bearer $token",
            timeRange = periodoSeleccionado,
            limit = 50
        ).enqueue(object : Callback<TopArtistsResponse> {
            override fun onResponse(
                call: Call<TopArtistsResponse>,
                response: Response<TopArtistsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val genreCounts = response.body()!!.items
                        .flatMap { it.genres }
                        .groupingBy { it }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .take(3)

                    val total = genreCounts.sumOf { it.value }
                    if (total == 0) {
                        mostrarSinGeneros()
                        return
                    }

                    val rows = listOf(
                        GenreRow(txtGenero1, txtGeneroPorcentaje1, progressGenero1),
                        GenreRow(txtGenero2, txtGeneroPorcentaje2, progressGenero2),
                        GenreRow(txtGenero3, txtGeneroPorcentaje3, progressGenero3)
                    )

                    rows.forEachIndexed { index, row ->
                        val genre = genreCounts.getOrNull(index)
                        if (genre == null) {
                            row.name.text = "-"
                            row.percent.text = "-"
                            row.progress.progress = 0
                        } else {
                            val percent = (genre.value * 100) / total
                            row.name.text = genre.key.replaceFirstChar { it.uppercase() }
                            row.percent.text = "$percent%"
                            row.progress.progress = percent
                        }
                    }
                } else {
                    mostrarErrorGeneros()
                }
            }

            override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                mostrarErrorGeneros()
            }
        })
    }

    private fun cargarVibes(token: String) {
        RetrofitClient.spotifyApiService.getTopTracks(
            authHeader = "Bearer $token",
            timeRange = periodoSeleccionado,
            limit = 50
        ).enqueue(object : Callback<TopTracksResponse> {
            override fun onResponse(
                call: Call<TopTracksResponse>,
                response: Response<TopTracksResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val tracks = response.body()!!.items
                    val ids = tracks
                        .map { it.id }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .joinToString(",")

                    if (ids.isBlank()) {
                        cargarVibesEstimadas(token, tracks)
                        return
                    }

                    cargarAudioFeatures(token, tracks, ids)
                } else {
                    cargarVibesEstimadas(token, emptyList())
                }
            }

            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                cargarVibesEstimadas(token, emptyList())
            }
        })
    }

    private fun cargarAudioFeatures(token: String, tracks: List<Track>, ids: String) {
        RetrofitClient.spotifyApiService.getAudioFeatures(
            authHeader = "Bearer $token",
            ids = ids
        ).enqueue(object : Callback<AudioFeaturesResponse> {
            override fun onResponse(
                call: Call<AudioFeaturesResponse>,
                response: Response<AudioFeaturesResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val features = response.body()!!.audio_features.filterNotNull()
                    val vibes = VibeCalculator.calcularVibes(
                        tracks = tracks,
                        features = features,
                        etiquetaRango = etiquetaRangoSeleccionado()
                    )
                    if (vibes.isEmpty()) {
                        cargarVibesEstimadas(token, tracks)
                    } else {
                        vibesAdapter.submitList(vibes)
                    }
                } else {
                    cargarVibesEstimadas(token, tracks)
                }
            }

            override fun onFailure(call: Call<AudioFeaturesResponse>, t: Throwable) {
                cargarVibesEstimadas(token, tracks)
            }
        })
    }

    private fun cargarVibesEstimadas(token: String, tracks: List<Track>) {
        val artistIds = tracks
            .flatMap { track -> track.artists.map { it.id } }
            .filter { it.isNotBlank() }
            .distinct()
            .take(50)
            .joinToString(",")

        if (artistIds.isNotBlank()) {
            cargarGenerosDeArtistasParaVibes(token, tracks, artistIds)
            return
        }

        cargarVibesEstimadasConTopArtists(token, tracks)
    }

    private fun cargarGenerosDeArtistasParaVibes(token: String, tracks: List<Track>, artistIds: String) {
        RetrofitClient.spotifyApiService.getArtists(
            authHeader = "Bearer $token",
            ids = artistIds
        ).enqueue(object : Callback<ArtistsResponse> {
            override fun onResponse(
                call: Call<ArtistsResponse>,
                response: Response<ArtistsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val artists = response.body()!!.artists
                    val genresByArtistId = artists.associate { it.id to it.genres }
                    val genres = artists.flatMap { it.genres }
                    val vibes = VibeCalculator.calcularVibesEstimadas(
                        tracks = tracks,
                        artistGenres = genres,
                        etiquetaRango = etiquetaRangoSeleccionado(),
                        artistGenresById = genresByArtistId
                    )
                    if (vibes.isEmpty()) mostrarSinVibes() else vibesAdapter.submitList(vibes)
                } else {
                    cargarVibesEstimadasConTopArtists(token, tracks)
                }
            }

            override fun onFailure(call: Call<ArtistsResponse>, t: Throwable) {
                cargarVibesEstimadasConTopArtists(token, tracks)
            }
        })
    }

    private fun cargarVibesEstimadasConTopArtists(token: String, tracks: List<Track>) {
        RetrofitClient.spotifyApiService.getTopArtists(
            authHeader = "Bearer $token",
            timeRange = periodoSeleccionado,
            limit = 50
        ).enqueue(object : Callback<TopArtistsResponse> {
            override fun onResponse(
                call: Call<TopArtistsResponse>,
                response: Response<TopArtistsResponse>
            ) {
                val genres = response.body()?.items?.flatMap { it.genres }.orEmpty()
                val vibes = VibeCalculator.calcularVibesEstimadas(
                    tracks = tracks,
                    artistGenres = genres,
                    etiquetaRango = etiquetaRangoSeleccionado()
                )
                if (vibes.isEmpty()) {
                    mostrarSinVibes()
                } else {
                    vibesAdapter.submitList(vibes)
                }
            }

            override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                val vibes = VibeCalculator.calcularVibesEstimadas(
                    tracks = tracks,
                    artistGenres = emptyList(),
                    etiquetaRango = etiquetaRangoSeleccionado()
                )
                if (vibes.isEmpty()) mostrarSinVibes() else vibesAdapter.submitList(vibes)
            }
        })
    }

    private fun mostrarCargaResumen() {
        txtMinutos.text = "-"
        txtHoras.text = "-"
        txtArtistas.text = "-"
        txtCanciones.text = "-"
    }

    private fun mostrarCargaGeneros() {
        listOf(txtGenero1, txtGenero2, txtGenero3).forEach { it.text = "-" }
        listOf(txtGeneroPorcentaje1, txtGeneroPorcentaje2, txtGeneroPorcentaje3).forEach { it.text = "-" }
        listOf(progressGenero1, progressGenero2, progressGenero3).forEach { it.progress = 0 }
    }

    private fun mostrarCargaVibes() {
        vibesAdapter.submitList(
            listOf(
                VibeCard(
                    id = "loading",
                    nombre = "Tus vibes",
                    porcentaje = 0,
                    icono = "...",
                    color = "#E0F2F1",
                    descripcion = "Analizando tus canciones top de ${etiquetaRangoSeleccionado()}",
                    ejemploCancion = "Cargando",
                    ejemploArtista = "Spotify",
                    imagenUrl = null
                )
            )
        )
    }

    private fun mostrarErrorResumen() {
        txtMinutos.text = "Error"
        txtHoras.text = "Error"
        txtArtistas.text = "Error"
        txtCanciones.text = "Error"
    }

    private fun mostrarErrorGeneros() {
        txtGenero1.text = "Error"
        txtGenero2.text = "-"
        txtGenero3.text = "-"
        txtGeneroPorcentaje1.text = "-"
        txtGeneroPorcentaje2.text = "-"
        txtGeneroPorcentaje3.text = "-"
        progressGenero1.progress = 0
        progressGenero2.progress = 0
        progressGenero3.progress = 0
    }

    private fun mostrarSinGeneros() {
        txtGenero1.text = "Sin datos"
        txtGenero2.text = "-"
        txtGenero3.text = "-"
        txtGeneroPorcentaje1.text = "-"
        txtGeneroPorcentaje2.text = "-"
        txtGeneroPorcentaje3.text = "-"
        progressGenero1.progress = 0
        progressGenero2.progress = 0
        progressGenero3.progress = 0
    }

    private fun mostrarSinVibes() {
        vibesAdapter.submitList(
            listOf(
                VibeCard(
                    id = "empty",
                    nombre = "Sin datos",
                    porcentaje = 0,
                    icono = "--",
                    color = "#ECEFF1",
                    descripcion = "No se han podido calcular tus vibes recientes",
                    ejemploCancion = "Sin ejemplo",
                    ejemploArtista = "Spotify",
                    imagenUrl = null
                )
            )
        )
    }

    private fun actualizarTituloVibes() {
        txtVibes.text = "Tus vibes de ${etiquetaRangoSeleccionado()}"
    }

    private fun etiquetaRangoSeleccionado(): String {
        return when (periodoSeleccionado) {
            "medium_term" -> "los últimos 6 meses"
            "long_term" -> "siempre"
            else -> "las últimas 4 semanas"
        }
    }

    private data class GenreRow(
        val name: TextView,
        val percent: TextView,
        val progress: ProgressBar
    )
}
