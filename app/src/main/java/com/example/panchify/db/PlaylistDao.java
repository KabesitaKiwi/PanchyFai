package com.example.panchify.db;

import com.example.panchify.modelos.CancionBD;
import com.example.panchify.modelos.Playlist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDao {

    public static int guardarPlaylistSpotify(String spotifyPlaylistId, String nombrePlaylist, String descripcion, int idUsuario) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        try {
            asegurarColumnas(conn);

            String selectSql = "SELECT idPlaylist FROM Playlist WHERE spotifyPlaylistId = ? AND idUsuario = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, spotifyPlaylistId);
            selectStmt.setInt(2, idUsuario);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int idPlaylist = rs.getInt("idPlaylist");
                String updateSql = "UPDATE Playlist SET nombrePlaylist = ?, descripcion = ? WHERE idPlaylist = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, valueOrDefault(nombrePlaylist, "Playlist"));
                updateStmt.setString(2, descripcion);
                updateStmt.setInt(3, idPlaylist);
                updateStmt.executeUpdate();
                return idPlaylist;
            }

            String insertSql = "INSERT INTO Playlist (spotifyPlaylistId, nombrePlaylist, descripcion, idUsuario) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, spotifyPlaylistId);
            insertStmt.setString(2, valueOrDefault(nombrePlaylist, "Playlist"));
            insertStmt.setString(3, descripcion);
            insertStmt.setInt(4, idUsuario);
            insertStmt.executeUpdate();

            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
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

        return -1;
    }

    public static boolean guardarCancionesPlaylistSpotify(
            int idUsuario,
            String spotifyPlaylistId,
            String nombrePlaylist,
            String descripcion,
            List<CancionBD> canciones
    ) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            asegurarColumnas(conn);
            conn.setAutoCommit(false);

            int idPlaylist = obtenerOcrearPlaylistEnConexion(conn, idUsuario, spotifyPlaylistId, nombrePlaylist, descripcion);
            if (idPlaylist == -1) {
                conn.rollback();
                return false;
            }

            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM CancionPlaylist WHERE idPlaylist = ?");
            deleteStmt.setInt(1, idPlaylist);
            deleteStmt.executeUpdate();

            for (CancionBD cancion : canciones) {
                guardarCancionEnConexion(conn, cancion);

                PreparedStatement relationStmt = conn.prepareStatement(
                        "INSERT INTO CancionPlaylist (idPlaylist, idCancion) VALUES (?, ?)"
                );
                relationStmt.setInt(1, idPlaylist);
                relationStmt.setString(2, cancion.getIdCancion());
                relationStmt.executeUpdate();
            }

            conn.commit();
            return true;
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

        return false;
    }

    public static List<Playlist> obtenerPlaylistsPorUsuario(int idUsuario) {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT p.*, COUNT(cp.idCancion) AS totalCanciones " +
                "FROM Playlist p " +
                "LEFT JOIN CancionPlaylist cp ON p.idPlaylist = cp.idPlaylist " +
                "WHERE p.idUsuario = ? " +
                "GROUP BY p.idPlaylist, p.spotifyPlaylistId, p.nombrePlaylist, p.descripcion, p.fechaCreacion, p.idUsuario " +
                "ORDER BY p.fechaCreacion DESC, p.idPlaylist DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                playlists.add(new Playlist(
                        rs.getInt("idPlaylist"),
                        getStringIfExists(rs, "spotifyPlaylistId"),
                        rs.getString("nombrePlaylist"),
                        getStringIfExists(rs, "descripcion"),
                        getStringIfExists(rs, "fechaCreacion"),
                        rs.getInt("idUsuario"),
                        rs.getInt("totalCanciones")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return playlists;
    }

    public static List<CancionBD> obtenerCancionesDePlaylist(int idPlaylist) {
        List<CancionBD> canciones = new ArrayList<>();

        String sql = "SELECT c.* " +
                "FROM CancionPlaylist cp " +
                "JOIN Cancion c ON cp.idCancion = c.idCancion " +
                "WHERE cp.idPlaylist = ? " +
                "ORDER BY cp.idRelacion ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPlaylist);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                canciones.add(new CancionBD(
                        rs.getString("idCancion"),
                        valueOrDefault(getStringIfExists(rs, "titulo"), "Cancion"),
                        getStringIfExists(rs, "artista"),
                        getStringIfExists(rs, "album"),
                        getStringIfExists(rs, "imagenUrl"),
                        getStringIfExists(rs, "previewUrl"),
                        getLongIfExists(rs, "duracionMs"),
                        getStringIfExists(rs, "anio"),
                        getStringIfExists(rs, "spotifyUrl")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return canciones;
    }

    private static String getStringIfExists(ResultSet rs, String columnName) {
        try {
            if (!hasColumn(rs, columnName)) return null;
            return rs.getString(columnName);
        } catch (SQLException e) {
            return null;
        }
    }

    private static Long getLongIfExists(ResultSet rs, String columnName) {
        try {
            if (!hasColumn(rs, columnName)) return null;
            long value = rs.getLong(columnName);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }

    private static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static void asegurarColumnas(Connection conn) {
        try { conn.createStatement().execute("ALTER TABLE Playlist ADD COLUMN spotifyPlaylistId VARCHAR(255)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Playlist ADD COLUMN descripcion TEXT"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Playlist ADD COLUMN fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Playlist ADD COLUMN idUsuario INT"); } catch (Exception e) {}
        try { conn.createStatement().execute("CREATE UNIQUE INDEX idx_playlist_usuario_spotify ON Playlist (idUsuario, spotifyPlaylistId)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Playlist ADD CONSTRAINT fk_playlist_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN artista VARCHAR(255)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN imagenUrl VARCHAR(255)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN previewUrl VARCHAR(255)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN album VARCHAR(255)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN duracionMs BIGINT"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN anio VARCHAR(10)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN spotifyUrl VARCHAR(255)"); } catch (Exception e) {}
    }

    private static int obtenerOcrearPlaylistEnConexion(
            Connection conn,
            int idUsuario,
            String spotifyPlaylistId,
            String nombrePlaylist,
            String descripcion
    ) throws SQLException {
        String selectSql = "SELECT idPlaylist FROM Playlist WHERE spotifyPlaylistId = ? AND idUsuario = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        selectStmt.setString(1, spotifyPlaylistId);
        selectStmt.setInt(2, idUsuario);
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            int idPlaylist = rs.getInt("idPlaylist");
            String updateSql = "UPDATE Playlist SET nombrePlaylist = ?, descripcion = ? WHERE idPlaylist = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, valueOrDefault(nombrePlaylist, "Playlist"));
            updateStmt.setString(2, descripcion);
            updateStmt.setInt(3, idPlaylist);
            updateStmt.executeUpdate();
            return idPlaylist;
        }

        String insertSql = "INSERT INTO Playlist (spotifyPlaylistId, nombrePlaylist, descripcion, idUsuario) VALUES (?, ?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, spotifyPlaylistId);
        insertStmt.setString(2, valueOrDefault(nombrePlaylist, "Playlist"));
        insertStmt.setString(3, descripcion);
        insertStmt.setInt(4, idUsuario);
        insertStmt.executeUpdate();

        ResultSet keys = insertStmt.getGeneratedKeys();
        return keys.next() ? keys.getInt(1) : -1;
    }

    private static void guardarCancionEnConexion(Connection conn, CancionBD cancion) throws SQLException {
        String selectSql = "SELECT idCancion FROM Cancion WHERE idCancion = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        selectStmt.setString(1, cancion.getIdCancion());
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            String updateSql = "UPDATE Cancion SET titulo = ?, artista = ?, imagenUrl = ?, previewUrl = ?, album = ?, duracionMs = ?, anio = ?, spotifyUrl = ? WHERE idCancion = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            rellenarCancionStatement(updateStmt, cancion, 1);
            updateStmt.setString(9, cancion.getIdCancion());
            updateStmt.executeUpdate();
            return;
        }

        String insertSql = "INSERT INTO Cancion (idCancion, titulo, artista, imagenUrl, previewUrl, album, duracionMs, anio, spotifyUrl) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql);
        insertStmt.setString(1, cancion.getIdCancion());
        rellenarCancionStatement(insertStmt, cancion, 2);
        insertStmt.executeUpdate();
    }

    private static void rellenarCancionStatement(PreparedStatement stmt, CancionBD cancion, int startIndex) throws SQLException {
        stmt.setString(startIndex, valueOrDefault(cancion.getTitulo(), "Cancion"));
        stmt.setString(startIndex + 1, cancion.getArtista());
        stmt.setString(startIndex + 2, cancion.getImagenUrl());
        stmt.setString(startIndex + 3, cancion.getPreviewUrl());
        stmt.setString(startIndex + 4, cancion.getAlbum());
        if (cancion.getDuracionMs() != null) {
            stmt.setLong(startIndex + 5, cancion.getDuracionMs());
        } else {
            stmt.setNull(startIndex + 5, Types.BIGINT);
        }
        stmt.setString(startIndex + 6, cancion.getAnio());
        stmt.setString(startIndex + 7, cancion.getSpotifyUrl());
    }
}
