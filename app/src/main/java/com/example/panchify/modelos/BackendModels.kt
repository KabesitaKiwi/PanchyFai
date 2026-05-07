package com.example.panchify.modelos

data class UsuarioRequest(
    val spotifyId: String,
    val nombreUsuario: String?,
    val email: String?,
    val imagenPerfil: String?
)

data class ComentarioRequest(
    val idUsuario: Int,
    val idCancion: String,
    val texto: String,
    val puntuacion: Int? = null,
    val tituloCancion: String? = null,
    val album: String? = null,
    val portada: String? = null,
    val urlSpotify: String? = null,
    val artistas: List<ArtistaSimple>? = null
)

data class ArtistaSimple(
    val idArtista: String,
    val nombre: String
)

data class MensajeResponse(
    val idMensaje: Int,
    val idEmisor: Int,
    val idReceptor: Int,
    val texto: String,
    val fecha: String?,
    val leido: Boolean,
    val nombreEmisor: String?,
    val imagenEmisor: String?
)

data class SolicitudAmistadResponse(
    val idSolicitud: Int,
    val fechaSolicitud: String?,
    val idUsuario: Int,
    val spotifyId: String?,
    val nombreUsuario: String?,
    val imagenPerfil: String?
)

data class AmigoResponse(
    val idUsuario: Int,
    val spotifyId: String?,
    val nombreUsuario: String?,
    val imagenPerfil: String?
)

data class GenericResponse(
    val mensaje: String?,
    val error: String?
)
