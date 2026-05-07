package com.example.panchify.adapters

import android.media.MediaPlayer
import android.util.Log
import com.example.panchify.api.SpotifyRemoteManager

object AudioPlayer {
    var mediaPlayer: MediaPlayer? = null
    var currentPlayingUrl: String? = null
    var currentPlayingId: String? = null

    fun playOrPause(trackId: String, previewUrl: String?, onStart: () -> Unit, onStop: () -> Unit, onError: (Exception) -> Unit) {
        
        // Si no hay preview URL (algo muy común ahora en Spotify), reproducimos la canción completa a través de Spotify
        if (previewUrl == null) {
            try {
                // Detenemos cualquier vista previa que estuviera sonando
                release()
                
                if (currentPlayingId == trackId) {
                    SpotifyRemoteManager.pause()
                    currentPlayingId = null
                    onStop()
                } else {
                    SpotifyRemoteManager.playTrack(trackId)
                    currentPlayingId = trackId
                    onStart()
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error con AppRemote: \${e.message}")
                onError(e)
            }
            return
        }

        // Si SÍ hay preview URL, usamos MediaPlayer
        if (currentPlayingUrl == previewUrl && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            onStop()
        } else if (currentPlayingUrl == previewUrl && mediaPlayer != null) {
            mediaPlayer?.start()
            onStart()
        } else {
            try {
                mediaPlayer?.release()
                SpotifyRemoteManager.pause() // Pausar AppRemote por si acaso
                
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(previewUrl)
                    prepareAsync()
                    setOnPreparedListener { 
                        start()
                        onStart()
                    }
                    setOnCompletionListener {
                        currentPlayingUrl = null
                        release()
                        mediaPlayer = null
                        onStop()
                    }
                }
                currentPlayingUrl = previewUrl
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error al reproducir: \${e.message}")
                onError(e)
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingUrl = null
    }
}
