package com.example.panchify.db;

import com.example.panchify.modelos.UsuarioResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDao {

    /**
     * Registra al usuario si no existe, o actualiza sus datos si ya existe.
     * Devuelve el objeto UsuarioResponse con su ID de base de datos.
     */
    public static UsuarioResponse registrarORecuperarUsuario(String spotifyId, String nombre, String email, String imagen) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try {
            // Comprobar si ya existe
            String selectSql = "SELECT * FROM Usuario WHERE spotifyUserId = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, spotifyId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // El usuario existe, actualizamos solo email e imagen (para no pisar su nombre personalizado)
                String updateSql = "UPDATE Usuario SET email = COALESCE(?, email), fotoPerfil = COALESCE(?, fotoPerfil) WHERE spotifyUserId = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, email);
                updateStmt.setString(2, imagen);
                updateStmt.setString(3, spotifyId);
                updateStmt.executeUpdate();

                // Obtener los datos actualizados
                return new UsuarioResponse(
                        rs.getInt("idUsuario"),
                        rs.getString("spotifyUserId"),
                        rs.getString("nombreUsuario"),
                        rs.getString("email"),
                        rs.getString("fotoPerfil"),
                        rs.getString("fechaRegistro")
                );

            } else {
                // El usuario no existe, lo insertamos
                String insertSql = "INSERT INTO Usuario (spotifyUserId, nombreUsuario, email, fotoPerfil) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, spotifyId);
                insertStmt.setString(2, nombre);
                insertStmt.setString(3, email);
                insertStmt.setString(4, imagen);
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                int newId = -1;
                if (generatedKeys.next()) {
                    newId = generatedKeys.getInt(1);
                }

                return new UsuarioResponse(
                        newId,
                        spotifyId,
                        nombre,
                        email,
                        imagen,
                        null
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new UsuarioResponse(-1, "ERROR", e.getMessage(), "ERROR", "ERROR", "ERROR");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Actualiza solo el nombre de usuario (cuando el usuario lo cambia en su perfil)
     */
    public static boolean actualizarNombreUsuario(int idUsuario, String nuevoNombre) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            String sql = "UPDATE Usuario SET nombreUsuario = ? WHERE idUsuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nuevoNombre);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * Obtiene los datos completos de un usuario por su ID
     */
    public static UsuarioResponse obtenerUsuario(int idUsuario) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try {
            String sql = "SELECT * FROM Usuario WHERE idUsuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UsuarioResponse(
                        rs.getInt("idUsuario"),
                        rs.getString("spotifyUserId"),
                        rs.getString("nombreUsuario"),
                        rs.getString("email"),
                        rs.getString("fotoPerfil"),
                        rs.getString("fechaRegistro")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return null;
    }
}
