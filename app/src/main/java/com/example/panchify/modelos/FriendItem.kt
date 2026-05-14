package com.example.panchify.modelos

data class FriendItem(
    val idSolicitud: Int?,
    val idUsuario: Int,
    val spotifyId: String?,
    val nombreUsuario: String?,
    val email: String?,
    val imagenPerfil: String?,
    val estado: String,
    val tipo: FriendItemType
)

enum class FriendItemType {
    AMIGO,
    SOLICITUD_RECIBIDA
}
