package com.example.panchify.modelos

data class Playlist(
    val idPlaylist: Int,
    val spotifyPlaylistId: String?,
    val nombrePlaylist: String,
    val descripcion: String?,
    val fechaCreacion: String?,
    val idUsuario: Int,
    val totalCanciones: Int = 0
)
