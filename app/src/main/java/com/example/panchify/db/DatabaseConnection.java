package com.example.panchify.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // =============================================
    // CAMBIA ESTOS VALORES POR LOS DE TU HOSTINGER
    // =============================================
    private static final String HOST = "auth-db1794.hstgr.io"; 
    private static final String DB_NAME = "u933199268_Panchify";
    private static final String USER = "u933199268_PanchiUser";
    private static final String PASS = "Zamorachinchipe0?";
    
    // El puerto por defecto de MySQL es 3306
    private static final String URL = "jdbc:mysql://" + HOST + ":3306/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Cargar el driver JDBC de MySQL
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Driver JDBC no encontrado.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al conectar con la base de datos.");
        }
        return connection;
    }
}
