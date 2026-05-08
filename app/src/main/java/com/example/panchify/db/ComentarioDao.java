package com.example.panchify.db;

import com.example.panchify.modelos.ComentarioResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ComentarioDao {

    public static List<ComentarioResponse> listarComentarios() {
        List<ComentarioResponse> lista = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return lista;

        try {
            // Migración de base de datos segura
            try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN imagenUrl VARCHAR(255)"); } catch (Exception e) {}
            try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN previewUrl VARCHAR(255)"); } catch (Exception e) {}

            String sql = "SELECT c.*, u.nombreUsuario, u.fotoPerfil, ca.titulo AS tituloCancion, ca.imagenUrl, ca.previewUrl " +
                         "FROM Comentario c " +
                         "JOIN Usuario u ON c.idUsuario = u.idUsuario " +
                         "LEFT JOIN Cancion ca ON c.idCancion = ca.idCancion " +
                         "ORDER BY c.fecha DESC LIMIT 50";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ComentarioResponse comentario = new ComentarioResponse(
                        rs.getInt("idComentario"),
                        rs.getString("texto"),
                        rs.getObject("puntuacion") != null ? (Integer) rs.getInt("puntuacion") : null,
                        rs.getString("fecha"),
                        rs.getInt("idUsuario"),
                        rs.getString("idCancion"),
                        rs.getString("nombreUsuario"),
                        rs.getString("fotoPerfil"),
                        rs.getString("tituloCancion"),
                        rs.getString("imagenUrl"),
                        rs.getString("previewUrl")
                );
                lista.add(comentario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return lista;
    }

    public static ComentarioResponse crearComentario(int idUsuario, String idCancion, String texto, Integer puntuacion, String tituloCancion, String imagenUrl, String previewUrl) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try {
            // Migración de base de datos segura
            try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN imagenUrl VARCHAR(255)"); } catch (Exception e) {}
            try { conn.createStatement().execute("ALTER TABLE Cancion ADD COLUMN previewUrl VARCHAR(255)"); } catch (Exception e) {}
            // Asegurarnos de que la canción exista
            String checkCancion = "SELECT idCancion FROM Cancion WHERE idCancion = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkCancion);
            checkStmt.setString(1, idCancion);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (!checkRs.next()) {
                String insertCancion = "INSERT INTO Cancion (idCancion, titulo, imagenUrl, previewUrl) VALUES (?, ?, ?, ?)";
                PreparedStatement insertCancionStmt = conn.prepareStatement(insertCancion);
                insertCancionStmt.setString(1, idCancion);
                insertCancionStmt.setString(2, tituloCancion != null ? tituloCancion : "Desconocido");
                insertCancionStmt.setString(3, imagenUrl);
                insertCancionStmt.setString(4, previewUrl);
                insertCancionStmt.executeUpdate();
            }

            // Insertar el comentario
            String insertSql = "INSERT INTO Comentario (texto, puntuacion, idUsuario, idCancion) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, texto);
            if (puntuacion != null) {
                insertStmt.setInt(2, puntuacion);
            } else {
                insertStmt.setNull(2, java.sql.Types.INTEGER);
            }
            insertStmt.setInt(3, idUsuario);
            insertStmt.setString(4, idCancion);
            insertStmt.executeUpdate();

            // Retornar un comentario básico sin joins complejos, 
            // aunque se puede volver a hacer SELECT si se necesita los datos de usuario al instante.
            return new ComentarioResponse(
                    0, texto, puntuacion, "Ahora", idUsuario, idCancion, "Tú", null, tituloCancion, imagenUrl, previewUrl
            );

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
