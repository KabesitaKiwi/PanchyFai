package com.example.panchify.vistas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.panchify.R
import com.example.panchify.config.SpotifyConfig

class Login : AppCompatActivity() {

    companion object {
        private const val CODIGO_SOLICITUD_SPOTIFY = 1337
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { vista, margenes ->
            val barrasSistema = margenes.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(
                barrasSistema.left,
                barrasSistema.top,
                barrasSistema.right,
                barrasSistema.bottom
            )
            margenes
        }

        // Procesar posible respuesta de Spotify al abrir la Activity
        intent?.let { procesarRespuestaSpotify(it) }

        val botonIniciarSesion = findViewById<Button>(R.id.btnLogin)
        botonIniciarSesion.setOnClickListener {
            iniciarSesionSpotify()
        }
    }

    private fun iniciarSesionSpotify() {
        val permisos = SpotifyConfig.SCOPES.joinToString(" ")

        val uriAutorizacion = Uri.parse("https://accounts.spotify.com/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", SpotifyConfig.CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter(
                "redirect_uri",
                SpotifyConfig.REDIRECT_URI
            )
            .appendQueryParameter("scope", permisos)
            .build()

        val intentoNavegador = Intent(Intent.ACTION_VIEW, uriAutorizacion)
        startActivity(intentoNavegador)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        procesarRespuestaSpotify(intent)
    }

    private fun procesarRespuestaSpotify(intento: Intent) {
        val datosRespuesta = intento.data
        if (datosRespuesta != null && datosRespuesta.scheme == "panchify") {
            val codigoAutorizacion = datosRespuesta.getQueryParameter("code")
            val error = datosRespuesta.getQueryParameter("error")

            if (codigoAutorizacion != null) {
                irAPantallaPrincipal()
            }
        }
    }

    private fun irAPantallaPrincipal() {
        val intentoPantallaPrincipal = Intent(this, Home::class.java)
        startActivity(intentoPantallaPrincipal)
        finish()
    }
}
