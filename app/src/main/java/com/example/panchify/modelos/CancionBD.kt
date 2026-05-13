package com.example.panchify.modelos

data class CancionBD(
    val idCancion: String,
    val titulo: String,
    val artista: String?,
    val album: String?,
    val imagenUrl: String?,
    val previewUrl: String?,
    val duracionMs: Long?,
    val anio: String?,
    val spotifyUrl: String?
)
