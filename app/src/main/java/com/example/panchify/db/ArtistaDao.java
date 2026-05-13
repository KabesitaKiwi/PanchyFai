package com.example.panchify.db;

import com.example.panchify.modelos.Artist;
import com.example.panchify.modelos.ArtistAppStats;
import com.example.panchify.modelos.ArtistFull;
import com.example.panchify.modelos.Track;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ArtistaDao {

    public static void guardarArtistas(List<ArtistFull> artistas) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            asegurarTabla(conn);
            for (ArtistFull artista : artistas) {
                guardarArtistaCompletoEnConexion(conn, artista);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void guardarArtistasBasicos(List<Artist> artistas) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            asegurarTabla(conn);
            for (Artist artista : artistas) {
                if (artista.getId() == null || artista.getId().trim().isEmpty()) continue;
                guardarArtistaBasicoEnConexion(conn, artista.getId(), artista.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void guardarRelacionesCancionArtistas(List<Track> tracks) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            asegurarTabla(conn);

            for (Track track : tracks) {
                if (track.getId() == null || track.getId().trim().isEmpty()) continue;

                for (Artist artista : track.getArtists()) {
                    if (artista.getId() == null || artista.getId().trim().isEmpty()) continue;

                    guardarArtistaBasicoEnConexion(conn, artista.getId(), artista.getName());

                    if (!existeRelacionCancionArtista(conn, track.getId(), artista.getId())) {
                        PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO CancionArtista (idCancion, idArtista) VALUES (?, ?)"
                        );
                        stmt.setString(1, track.getId());
                        stmt.setString(2, artista.getId());
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArtistAppStats obtenerEstadisticasArtistaUsuario(String idArtista, int idUsuario) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return new ArtistAppStats(0, 0);

        try {
            asegurarTabla(conn);

            String sql = "SELECT COUNT(DISTINCT cp.idCancion) AS canciones, " +
                    "COUNT(DISTINCT cp.idPlaylist) AS playlists " +
                    "FROM CancionArtista ca " +
                    "JOIN CancionPlaylist cp ON ca.idCancion = cp.idCancion " +
                    "JOIN Playlist p ON cp.idPlaylist = p.idPlaylist " +
                    "WHERE ca.idArtista = ? AND p.idUsuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idArtista);
            stmt.setInt(2, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ArtistAppStats(
                        rs.getInt("canciones"),
                        rs.getInt("playlists")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return new ArtistAppStats(0, 0);
    }

    public static void asegurarTabla(Connection conn) {
        try {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS Artista (" +
                            "idArtista VARCHAR(255) PRIMARY KEY, " +
                            "nombre VARCHAR(255), " +
                            "generoPrincipal VARCHAR(255), " +
                            "popularidad INT, " +
                            "imagen VARCHAR(255), " +
                            "urlSpotify VARCHAR(255)" +
                            ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS CancionArtista (" +
                            "idRelacion INT AUTO_INCREMENT PRIMARY KEY, " +
                            "idCancion VARCHAR(255), " +
                            "idArtista VARCHAR(255)" +
                            ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        try { conn.createStatement().execute("ALTER TABLE CancionArtista ADD COLUMN idRelacion INT AUTO_INCREMENT PRIMARY KEY FIRST"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE CancionArtista ADD COLUMN idCancion VARCHAR(255)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE CancionArtista ADD COLUMN idArtista VARCHAR(255)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Artista ADD COLUMN spotifyArtistId VARCHAR(255)"); } catch (Exception e) {}
        try { conn.createStatement().execute("CREATE UNIQUE INDEX idx_artista_spotify ON Artista (spotifyArtistId)"); } catch (Exception e) {}
        try { conn.createStatement().execute("CREATE UNIQUE INDEX idx_cancion_artista_unica ON CancionArtista (idCancion, idArtista)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE CancionArtista ADD CONSTRAINT fk_cancion_artista_cancion FOREIGN KEY (idCancion) REFERENCES Cancion(idCancion)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE CancionArtista ADD CONSTRAINT fk_cancion_artista_artista FOREIGN KEY (idArtista) REFERENCES Artista(idArtista)"); } catch (Exception e) {}
    }

    private static void guardarArtistaCompletoEnConexion(Connection conn, ArtistFull artista) throws SQLException {
        String generoPrincipal = artista.getGenres().isEmpty() ? null : artista.getGenres().get(0);
        String imagen = artista.getImages().isEmpty() ? null : artista.getImages().get(0).getUrl();
        String urlSpotify = artista.getExternal_urls() != null ? artista.getExternal_urls().getSpotify() : null;
        if (existeArtista(conn, artista.getId())) {
            String updateSql = "UPDATE Artista SET spotifyArtistId = ?, nombre = ?, generoPrincipal = ?, popularidad = ?, imagen = ?, urlSpotify = ? WHERE idArtista = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, artista.getId());
            stmt.setString(2, artista.getName());
            stmt.setString(3, generoPrincipal);
            stmt.setInt(4, artista.getPopularity());
            stmt.setString(5, imagen);
            stmt.setString(6, urlSpotify);
            stmt.setString(7, artista.getId());
            stmt.executeUpdate();
        } else {
            String insertSql = "INSERT INTO Artista (idArtista, spotifyArtistId, nombre, generoPrincipal, popularidad, imagen, urlSpotify) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertSql);
            stmt.setString(1, artista.getId());
            stmt.setString(2, artista.getId());
            stmt.setString(3, artista.getName());
            stmt.setString(4, generoPrincipal);
            stmt.setInt(5, artista.getPopularity());
            stmt.setString(6, imagen);
            stmt.setString(7, urlSpotify);
            stmt.executeUpdate();
        }
    }

    private static void guardarArtistaBasicoEnConexion(Connection conn, String spotifyArtistId, String nombre) throws SQLException {
        if (existeArtista(conn, spotifyArtistId)) {
            String updateSql = "UPDATE Artista SET spotifyArtistId = ?, nombre = COALESCE(nombre, ?) WHERE idArtista = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, spotifyArtistId);
            stmt.setString(2, nombre);
            stmt.setString(3, spotifyArtistId);
            stmt.executeUpdate();
        } else {
            String insertSql = "INSERT INTO Artista (idArtista, spotifyArtistId, nombre) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertSql);
            stmt.setString(1, spotifyArtistId);
            stmt.setString(2, spotifyArtistId);
            stmt.setString(3, nombre);
            stmt.executeUpdate();
        }
    }

    private static boolean existeRelacionCancionArtista(Connection conn, String idCancion, String idArtista) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT idRelacion FROM CancionArtista WHERE idCancion = ? AND idArtista = ?"
        );
        stmt.setString(1, idCancion);
        stmt.setString(2, idArtista);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    private static boolean existeArtista(Connection conn, String idArtista) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT idArtista FROM Artista WHERE idArtista = ?");
        stmt.setString(1, idArtista);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }
}
