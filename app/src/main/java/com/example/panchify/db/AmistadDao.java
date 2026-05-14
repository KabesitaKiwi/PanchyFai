package com.example.panchify.db;

import com.example.panchify.modelos.FriendItem;
import com.example.panchify.modelos.FriendItemType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AmistadDao {

    public static List<FriendItem> obtenerAmigosYSolicitudes(int idUsuario) {
        List<FriendItem> items = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return items;

        try {
            asegurarTabla(conn);
            items.addAll(obtenerSolicitudesRecibidas(conn, idUsuario));
            items.addAll(obtenerAmigos(conn, idUsuario));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return items;
    }

    public static boolean enviarSolicitud(int idEmisor, String busquedaReceptor) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            asegurarTabla(conn);

            Integer idReceptor = buscarUsuario(conn, busquedaReceptor, idEmisor);
            if (idReceptor == null || idReceptor == idEmisor) return false;
            if (existeRelacion(conn, idEmisor, idReceptor)) return false;

            String sql = "INSERT INTO SolicitudAmistad (idEmisor, idReceptor, estado) VALUES (?, ?, 'pendiente')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idEmisor);
            stmt.setInt(2, idReceptor);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean enviarSolicitudAUsuario(int idEmisor, int idReceptor) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            asegurarTabla(conn);
            if (idReceptor == idEmisor) return false;
            Integer idSolicitudRechazada = obtenerSolicitudRechazada(conn, idEmisor, idReceptor);
            if (idSolicitudRechazada != null) {
                String updateSql = "UPDATE SolicitudAmistad SET idEmisor = ?, idReceptor = ?, estado = 'pendiente', fechaSolicitud = CURRENT_TIMESTAMP WHERE idSolicitud = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, idEmisor);
                updateStmt.setInt(2, idReceptor);
                updateStmt.setInt(3, idSolicitudRechazada);
                return updateStmt.executeUpdate() > 0;
            }
            if (existeRelacionActiva(conn, idEmisor, idReceptor)) return false;

            String sql = "INSERT INTO SolicitudAmistad (idEmisor, idReceptor, estado) VALUES (?, ?, 'pendiente')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idEmisor);
            stmt.setInt(2, idReceptor);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<FriendItem> obtenerUsuariosParaInvitar(int idUsuario) {
        List<FriendItem> usuarios = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return usuarios;

        try {
            asegurarTabla(conn);

            String sql = "SELECT u.*, " +
                    "(SELECT s.estado FROM SolicitudAmistad s " +
                    "WHERE (s.idEmisor = ? AND s.idReceptor = u.idUsuario) " +
                    "OR (s.idEmisor = u.idUsuario AND s.idReceptor = ?) " +
                    "ORDER BY s.idSolicitud DESC LIMIT 1) AS estadoRelacion " +
                    "FROM Usuario u " +
                    "WHERE u.idUsuario <> ? " +
                    "ORDER BY u.nombreUsuario ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idUsuario);
            stmt.setInt(3, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                usuarios.add(new FriendItem(
                        null,
                        rs.getInt("idUsuario"),
                        rs.getString("spotifyUserId"),
                        rs.getString("nombreUsuario"),
                        rs.getString("email"),
                        rs.getString("fotoPerfil"),
                        rs.getString("estadoRelacion") == null ? "disponible" : rs.getString("estadoRelacion"),
                        FriendItemType.AMIGO
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

        return usuarios;
    }

    public static boolean responderSolicitud(int idSolicitud, int idUsuario, boolean aceptar) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            asegurarTabla(conn);

            String sql = "UPDATE SolicitudAmistad SET estado = ? WHERE idSolicitud = ? AND idReceptor = ? AND estado = 'pendiente'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, aceptar ? "aceptada" : "rechazada");
            stmt.setInt(2, idSolicitud);
            stmt.setInt(3, idUsuario);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<FriendItem> obtenerSolicitudesRecibidas(Connection conn, int idUsuario) throws SQLException {
        List<FriendItem> items = new ArrayList<>();
        String sql = "SELECT s.idSolicitud, s.estado, u.* " +
                "FROM SolicitudAmistad s " +
                "JOIN Usuario u ON s.idEmisor = u.idUsuario " +
                "WHERE s.idReceptor = ? AND s.estado = 'pendiente' " +
                "ORDER BY s.fechaSolicitud DESC, s.idSolicitud DESC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idUsuario);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            items.add(mapFriendItem(rs, FriendItemType.SOLICITUD_RECIBIDA));
        }
        return items;
    }

    private static List<FriendItem> obtenerAmigos(Connection conn, int idUsuario) throws SQLException {
        List<FriendItem> items = new ArrayList<>();
        String sql = "SELECT s.idSolicitud, s.estado, u.* " +
                "FROM SolicitudAmistad s " +
                "JOIN Usuario u ON u.idUsuario = CASE WHEN s.idEmisor = ? THEN s.idReceptor ELSE s.idEmisor END " +
                "WHERE s.estado = 'aceptada' AND (s.idEmisor = ? OR s.idReceptor = ?) " +
                "ORDER BY u.nombreUsuario ASC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idUsuario);
        stmt.setInt(2, idUsuario);
        stmt.setInt(3, idUsuario);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            items.add(mapFriendItem(rs, FriendItemType.AMIGO));
        }
        return items;
    }

    private static FriendItem mapFriendItem(ResultSet rs, FriendItemType tipo) throws SQLException {
        return new FriendItem(
                rs.getInt("idSolicitud"),
                rs.getInt("idUsuario"),
                rs.getString("spotifyUserId"),
                rs.getString("nombreUsuario"),
                rs.getString("email"),
                rs.getString("fotoPerfil"),
                rs.getString("estado"),
                tipo
        );
    }

    private static Integer buscarUsuario(Connection conn, String busqueda, int idUsuarioActual) throws SQLException {
        String texto = busqueda == null ? "" : busqueda.trim();
        if (texto.isEmpty()) return null;

        String sql = "SELECT idUsuario FROM Usuario " +
                "WHERE idUsuario <> ? AND (LOWER(nombreUsuario) = LOWER(?) OR LOWER(email) = LOWER(?) OR spotifyUserId = ?) " +
                "LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idUsuarioActual);
        stmt.setString(2, texto);
        stmt.setString(3, texto);
        stmt.setString(4, texto);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("idUsuario") : null;
    }

    private static boolean existeRelacion(Connection conn, int idA, int idB) throws SQLException {
        String sql = "SELECT idSolicitud FROM SolicitudAmistad " +
                "WHERE (idEmisor = ? AND idReceptor = ?) OR (idEmisor = ? AND idReceptor = ?) " +
                "LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idA);
        stmt.setInt(2, idB);
        stmt.setInt(3, idB);
        stmt.setInt(4, idA);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    private static boolean existeRelacionActiva(Connection conn, int idA, int idB) throws SQLException {
        String sql = "SELECT idSolicitud FROM SolicitudAmistad " +
                "WHERE ((idEmisor = ? AND idReceptor = ?) OR (idEmisor = ? AND idReceptor = ?)) " +
                "AND estado IN ('pendiente', 'aceptada') " +
                "LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idA);
        stmt.setInt(2, idB);
        stmt.setInt(3, idB);
        stmt.setInt(4, idA);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    private static Integer obtenerSolicitudRechazada(Connection conn, int idA, int idB) throws SQLException {
        String sql = "SELECT idSolicitud FROM SolicitudAmistad " +
                "WHERE ((idEmisor = ? AND idReceptor = ?) OR (idEmisor = ? AND idReceptor = ?)) " +
                "AND estado = 'rechazada' " +
                "ORDER BY idSolicitud DESC LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idA);
        stmt.setInt(2, idB);
        stmt.setInt(3, idB);
        stmt.setInt(4, idA);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("idSolicitud") : null;
    }

    private static void asegurarTabla(Connection conn) {
        try {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS SolicitudAmistad (" +
                            "idSolicitud INT AUTO_INCREMENT PRIMARY KEY, " +
                            "idEmisor INT NOT NULL, " +
                            "idReceptor INT NOT NULL, " +
                            "estado VARCHAR(20) NOT NULL DEFAULT 'pendiente', " +
                            "fechaSolicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        try { conn.createStatement().execute("ALTER TABLE SolicitudAmistad ADD COLUMN idSolicitud INT AUTO_INCREMENT PRIMARY KEY FIRST"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE SolicitudAmistad ADD COLUMN idEmisor INT NOT NULL"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE SolicitudAmistad ADD COLUMN idReceptor INT NOT NULL"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE SolicitudAmistad ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'pendiente'"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE SolicitudAmistad ADD COLUMN fechaSolicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP"); } catch (Exception e) {}
        try { conn.createStatement().execute("CREATE INDEX idx_solicitud_receptor_estado ON SolicitudAmistad (idReceptor, estado)"); } catch (Exception e) {}
        try { conn.createStatement().execute("CREATE INDEX idx_solicitud_emisor_estado ON SolicitudAmistad (idEmisor, estado)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE SolicitudAmistad ADD CONSTRAINT fk_solicitud_emisor FOREIGN KEY (idEmisor) REFERENCES Usuario(idUsuario)"); } catch (Exception e) {}
        try { conn.createStatement().execute("ALTER TABLE SolicitudAmistad ADD CONSTRAINT fk_solicitud_receptor FOREIGN KEY (idReceptor) REFERENCES Usuario(idUsuario)"); } catch (Exception e) {}
    }
}
