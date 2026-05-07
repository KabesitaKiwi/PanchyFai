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
            String selectSql = "SELECT * FROM Usuario WHERE spotifyId = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, spotifyId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // El usuario existe, lo actualizamos
                String updateSql = "UPDATE Usuario SET nombreUsuario = COALESCE(?, nombreUsuario), email = COALESCE(?, email), imagenPerfil = COALESCE(?, imagenPerfil) WHERE spotifyId = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, nombre);
                updateStmt.setString(2, email);
                updateStmt.setString(3, imagen);
                updateStmt.setString(4, spotifyId);
                updateStmt.executeUpdate();

                // Obtener los datos actualizados
                return new UsuarioResponse(
                        rs.getInt("idUsuario"),
                        rs.getString("spotifyId"),
                        rs.getString("nombreUsuario"),
                        rs.getString("email"),
                        rs.getString("imagenPerfil"),
                        rs.getString("fechaRegistro")
                );

            } else {
                // El usuario no existe, lo insertamos
                String insertSql = "INSERT INTO Usuario (spotifyId, nombreUsuario, email, imagenPerfil) VALUES (?, ?, ?, ?)";
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
