package com.example.panchify.db;

import com.example.panchify.modelos.Album;
import com.example.panchify.modelos.Artist;
import com.example.panchify.modelos.Image;
import com.example.panchify.modelos.Track;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopUsuarioDao {

    public static void guardarTopCanciones(int idUsuario, List<Track> tracks, String periodo) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            asegurarTabla(conn);
            conn.setAutoCommit(false);

            PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM TopCancionUsuario WHERE idUsuario = ? AND periodo = ?"
            );
            deleteStmt.setInt(1, idUsuario);
            deleteStmt.setString(2, periodo);
            deleteStmt.executeUpdate();

            String insertSql = "INSERT INTO TopCancionUsuario " +
                    "(idUsuario, periodo, posicion, idCancion, titulo, artistas, album, imagenUrl, previewUrl) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                insertStmt.setInt(1, idUsuario);
                insertStmt.setString(2, periodo);
                insertStmt.setInt(3, i + 1);
                insertStmt.setString(4, track.getId());
                insertStmt.setString(5, track.getName());
                insertStmt.setString(6, joinArtists(track.getArtists()));
                insertStmt.setString(7, track.getAlbum().getName());
                insertStmt.setString(8, track.getAlbum().getImages().isEmpty() ? null : track.getAlbum().getImages().get(0).getUrl());
                insertStmt.setString(9, track.getPreview_url());
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Track> obtenerTopCanciones(int idUsuario, String periodo, int limite) {
        List<Track> tracks = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return tracks;

        try {
            asegurarTabla(conn);
            String sql = "SELECT * FROM TopCancionUsuario WHERE idUsuario = ? AND periodo = ? ORDER BY posicion ASC LIMIT ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            stmt.setString(2, periodo);
            stmt.setInt(3, limite);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String imageUrl = rs.getString("imagenUrl");
                List<Image> images = imageUrl == null || imageUrl.trim().isEmpty()
                        ? Collections.emptyList()
                        : Collections.singletonList(new Image(imageUrl));
                tracks.add(new Track(
                        rs.getString("idCancion"),
                        rs.getString("titulo"),
                        Collections.singletonList(new Artist("", rs.getString("artistas"))),
                        new Album(rs.getString("album"), images, null),
                        0,
                        0,
                        rs.getString("previewUrl"),
                        null
                ));
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

        return tracks;
    }

    private static void asegurarTabla(Connection conn) {
        try {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS TopCancionUsuario (" +
                            "idTop INT AUTO_INCREMENT PRIMARY KEY, " +
                            "idUsuario INT NOT NULL, " +
                            "periodo VARCHAR(30) NOT NULL, " +
                            "posicion INT NOT NULL, " +
                            "idCancion VARCHAR(255) NOT NULL, " +
                            "titulo VARCHAR(255), " +
                            "artistas VARCHAR(255), " +
                            "album VARCHAR(255), " +
                            "imagenUrl VARCHAR(255), " +
                            "previewUrl VARCHAR(255), " +
                            "fechaActualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        try { conn.createStatement().execute("CREATE UNIQUE INDEX idx_top_usuario_periodo_posicion ON TopCancionUsuario (idUsuario, periodo, posicion)"); } catch (Exception e) {}
        try { conn.createStatement().execute("CREATE INDEX idx_top_usuario_periodo ON TopCancionUsuario (idUsuario, periodo)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE TopCancionUsuario ADD CONSTRAINT fk_top_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario)"); } catch (Exception e) {}
    }

    private static String joinArtists(List<Artist> artists) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < artists.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(artists.get(i).getName());
        }
        return builder.toString();
    }
}
