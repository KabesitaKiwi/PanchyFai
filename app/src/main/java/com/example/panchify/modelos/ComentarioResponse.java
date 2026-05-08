package com.example.panchify.modelos;

public class ComentarioResponse {
    private int idComentario;
    private String texto;
    private Integer puntuacion;
    private String fecha;
    private int idUsuario;
    private String idCancion;
    private String nombreUsuario;
    private String imagenPerfil;
    private String tituloCancion;
    private String imagenCancion;
    private String previewUrl;

    public ComentarioResponse(int idComentario, String texto, Integer puntuacion, String fecha, int idUsuario, String idCancion, String nombreUsuario, String imagenPerfil, String tituloCancion, String imagenCancion, String previewUrl) {
        this.idComentario = idComentario;
        this.texto = texto;
        this.puntuacion = puntuacion;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.idCancion = idCancion;
        this.nombreUsuario = nombreUsuario;
        this.imagenPerfil = imagenPerfil;
        this.tituloCancion = tituloCancion;
        this.imagenCancion = imagenCancion;
        this.previewUrl = previewUrl;
    }

    public int getIdComentario() { return idComentario; }
    public String getTexto() { return texto; }
    public Integer getPuntuacion() { return puntuacion; }
    public String getFecha() { return fecha; }
    public int getIdUsuario() { return idUsuario; }
    public String getIdCancion() { return idCancion; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getImagenPerfil() { return imagenPerfil; }
    public String getTituloCancion() { return tituloCancion; }
    public String getImagenCancion() { return imagenCancion; }
    public String getPreviewUrl() { return previewUrl; }
}
