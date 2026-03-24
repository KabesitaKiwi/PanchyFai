package com.example.panchify.vistas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.panchify.R
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.TopArtistsResponse
import com.example.panchify.modelos.TopTracksResponse
import com.example.panchify.preferences.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Stats : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    // TextViews de las 4 tarjetas
    private lateinit var txtMinutos: TextView
    private lateinit var txtHoras: TextView
    private lateinit var txtArtistas: TextView
    private lateinit var txtCanciones: TextView

    private var periodoSeleccionado = "short_term"

    // Guardamos resultados parciales de las 2 llamadas
    private var minutosCargados: Long = -1
    private var artistasCargados: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        sessionManager = SessionManager(this)

        txtMinutos   = findViewById(R.id.txtStatMinutos)
        txtHoras     = findViewById(R.id.txtStatHoras)
        txtArtistas  = findViewById(R.id.txtStatArtistas)
        txtCanciones = findViewById(R.id.txtStatCanciones)

        // ── Bottom Navigation ──────────────────────────────────────────────────
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_stats
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

        // ── Selector de tiempo ────────────────────────────────────────────────
        val timeSelector = findViewById<MaterialButtonToggleGroup>(R.id.timeSelector)
        timeSelector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                periodoSeleccionado = when (checkedId) {
                    R.id.boton4semanasStats -> "short_term"
                    R.id.boton6mesesStats   -> "medium_term"
                    R.id.botonSiempreStats  -> "long_term"
                    else -> "short_term"
                }
                cargarEstadisticas()
            }
        }

        // Carga inicial
        cargarEstadisticas()
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_stats)?.isChecked = true
    }

    private fun cargarEstadisticas() {
        val token = sessionManager.getAccessToken() ?: return

        // Resetear mientras carga
        minutosCargados  = -1
        artistasCargados = -1
        txtMinutos.text   = "—"
        txtHoras.text     = "—"
        txtArtistas.text  = "—"
        txtCanciones.text = "—"

        // ── Llamada 1: Top Tracks (minutos + canciones) ───────────────────────
        RetrofitClient.spotifyApiService.getTopTracks(
            authHeader = "Bearer $token",
            timeRange  = periodoSeleccionado,
            limit      = 50
        ).enqueue(object : retrofit2.Callback<TopTracksResponse> {
            override fun onResponse(call: retrofit2.Call<TopTracksResponse>, response: retrofit2.Response<TopTracksResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val tracks = response.body()!!.items
                    txtCanciones.text = tracks.size.toString()

                    val totalMs = tracks.sumOf { it.duration_ms }
                    val minutos = totalMs / 60_000
                    val horas   = totalMs / 3_600_000

                    txtMinutos.text = "%,d".format(minutos)
                    txtHoras.text   = horas.toString()
                    minutosCargados = minutos
                }
            }
            override fun onFailure(call: retrofit2.Call<TopTracksResponse>, t: Throwable) {
                txtMinutos.text   = "Error"
                txtHoras.text     = "Error"
                txtCanciones.text = "Error"
            }
        })

        // ── Llamada 2: Top Artists (número de artistas) ───────────────────────
        RetrofitClient.spotifyApiService.getTopArtists(
            authHeader = "Bearer $token",
            timeRange  = periodoSeleccionado,
            limit      = 50
        ).enqueue(object : retrofit2.Callback<TopArtistsResponse> {
            override fun onResponse(call: retrofit2.Call<TopArtistsResponse>, response: retrofit2.Response<TopArtistsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    artistasCargados = response.body()!!.items.size
                    txtArtistas.text = artistasCargados.toString()
                }
            }
            override fun onFailure(call: retrofit2.Call<TopArtistsResponse>, t: Throwable) {
                txtArtistas.text = "Error"
            }
        })
    }
}
