package com.example.panchify.modelos;

public class UsuarioResponse {
    private int idUsuario;
    private String spotifyId;
    private String nombreUsuario;
    private String email;
    private String imagenPerfil;
    private String fechaRegistro;

    public UsuarioResponse(int idUsuario, String spotifyId, String nombreUsuario, String email, String imagenPerfil, String fechaRegistro) {
        this.idUsuario = idUsuario;
        this.spotifyId = spotifyId;
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.imagenPerfil = imagenPerfil;
        this.fechaRegistro = fechaRegistro;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getSpotifyId() { return spotifyId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getEmail() { return email; }
    public String getImagenPerfil() { return imagenPerfil; }
    public String getFechaRegistro() { return fechaRegistro; }
}
