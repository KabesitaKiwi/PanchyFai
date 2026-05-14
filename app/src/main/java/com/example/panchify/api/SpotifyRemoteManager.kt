package com.example.panchify.api

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

object SpotifyRemoteManager {
    private const val CLIENT_ID = "7169289ba7de4350b0ef5105ace1e25f"
    private const val REDIRECT_URI = "panchify://callback"
    var appRemote: SpotifyAppRemote? = null

    fun connect(context: Context, onConnected: ((SpotifyAppRemote) -> Unit)? = null) {
        if (appRemote?.isConnected == true) {
            onConnected?.invoke(appRemote!!)
            return
        }

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context.applicationContext, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(remote: SpotifyAppRemote) {
                Log.d("SpotifyRemoteManager", "Conectado a Spotify")
                appRemote = remote
                onConnected?.invoke(remote)
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyRemoteManager", "Error al conectar a Spotify", throwable)
            }
        })
    }

    fun disconnect() {
        appRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
        appRemote = null
    }

    fun playTrack(trackId: String) {
        appRemote?.playerApi?.play("spotify:track:$trackId")
    }

    fun playPlaylist(playlistId: String) {
        appRemote?.playerApi?.play("spotify:playlist:$playlistId")
    }

    fun pause() {
        appRemote?.playerApi?.pause()
    }
}
