package com.licencias.sistemalicenciasfx.config;

import com.licencias.sistemalicenciasfx.model.exceptions.BaseDatosException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configuración de conexión a Supabase (PostgreSQL).
 * LISTO PARA USAR.
 */
public class DatabaseConfig {

    private static DatabaseConfig instancia;
    private final String url;
    private final String usuario;
    private final String password;
    private final String driver;

    private DatabaseConfig() {

        // Driver de PostgreSQL
        this.driver = "org.postgresql.Driver";
        String host = "aws-1-us-east-2.pooler.supabase.com";
        String dbName = "postgres";

        // URL JDBC (Usamos el puerto 5432 para conexión directa)
        this.url = "jdbc:postgresql://" + host + ":5432/" + dbName + "?sslmode=require";

        // CREDENCIALES
        this.usuario = "postgres.sbxndvnhvwdppcgomkda";
        this.password = "ContraseñaSegura123"; // ¡Tu contraseña!

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println("Error crítico: No se encontró el driver de PostgreSQL.");
            e.printStackTrace();
        }
    }
    // Método para obtener la instancia única (Singleton)
    public static synchronized DatabaseConfig getInstance() {
        if (instancia == null) {
            instancia = new DatabaseConfig();
        }
        return instancia;
    }
    // Método para conectar a la base de datos
    public Connection obtenerConexion() throws BaseDatosException {
        try {
            Connection conexion = DriverManager.getConnection(url, usuario, password);
            conexion.setAutoCommit(true);
            return conexion;
        } catch (SQLException e) {
            throw new BaseDatosException(
                    "Error al conectar con Supabase: " + e.getMessage(), e
            );
        }
    }
    // Método para cerrar la conexión y liberar recursos
    public void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }
}
