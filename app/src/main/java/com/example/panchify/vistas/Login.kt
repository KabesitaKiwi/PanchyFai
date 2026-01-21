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
import com.example.panchify.api.RetrofitClient
import com.example.panchify.config.SpotifyConfig
import com.example.panchify.modelos.TokenResponse
import com.example.panchify.preferences.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

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

        // Por si la Activity se abre desde el redirect
        intent?.let { procesarRespuestaSpotify(it) }

        val botonIniciarSesion = findViewById<Button>(R.id.btnLogin)
        botonIniciarSesion.setOnClickListener {
            iniciarSesionSpotify()
        }
    }

    /**
     * Abre el navegador con el login de Spotify
     */
    private fun iniciarSesionSpotify() {
        val permisos = SpotifyConfig.SCOPES.joinToString(" ")

        val uriAutorizacion = Uri.parse("https://accounts.spotify.com/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", SpotifyConfig.CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", SpotifyConfig.REDIRECT_URI)
            .appendQueryParameter("scope", permisos)
            .build()

        startActivity(Intent(Intent.ACTION_VIEW, uriAutorizacion))
    }

    /**
     * Se llama cuando volvemos del navegador (singleTask)
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        procesarRespuestaSpotify(intent)
    }

    /**
     * Lee el code que devuelve Spotify
     */
    private fun procesarRespuestaSpotify(intent: Intent) {
        val datos = intent.data
        if (datos != null && datos.scheme == "panchify") {
            val code = datos.getQueryParameter("code")
            if (code != null) {
                intercambiarCodigoPorToken(code)
            }
        }
    }

    /**
     * Intercambia el authorization code por el token REAL
     */
    private fun intercambiarCodigoPorToken(code: String) {

        RetrofitClient.spotifyAuthService.getToken(
            grantType = "authorization_code",
            code = code,
            redirectUri = SpotifyConfig.REDIRECT_URI,
            clientId = SpotifyConfig.CLIENT_ID,
            clientSecret = SpotifyConfig.CLIENT_SECRET
        ).enqueue(object : Callback<TokenResponse> {

            override fun onResponse(
                call: Call<TokenResponse>,
                response: Response<TokenResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    val token = response.body()!!

                    SessionManager(this@Login).saveToken(
                        token.access_token,
                        token.refresh_token,
                        token.expires_in
                    )

                    irAPantallaPrincipal()
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                // Error de red (puedes loguearlo o mostrar mensaje)
            }
        })
    }

    /**
     * Navega al Home cuando el login es correcto
     */
    private fun irAPantallaPrincipal() {
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}
