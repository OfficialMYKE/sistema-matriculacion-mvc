package com.licencias.sistemalicenciasfx.config;

import com.licencias.sistemalicenciasfx.model.exceptions.BaseDatosException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static DatabaseConfig instancia;
    private final String url;
    private final String usuario;
    private final String password;

    private DatabaseConfig() {
        // 1. CONFIGURACIÓN CORRECTA PARA TU PROYECTO (sbxndvnhvwdppcgomkda)

        // Host directo a tu base de datos
        String host = "db.sbxndvnhvwdppcgomkda.supabase.co";
        String puerto = "5432";
        String dbName = "postgres";

        // URL JDBC Estándar
        this.url = "jdbc:postgresql://" + host + ":" + puerto + "/" + dbName + "?sslmode=require";

        // 2. CREDENCIALES

        // El usuario para conexión directa SIEMPRE es 'postgres'
        this.usuario = "postgres";

        // Tu contraseña confirmada
        this.password = "ContraseñaSegura123";

        // Cargar Driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver de PostgreSQL.");
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instancia == null) {
            instancia = new DatabaseConfig();
        }
        return instancia;
    }

    public Connection obtenerConexion() throws BaseDatosException {
        try {
            Connection conexion = DriverManager.getConnection(url, usuario, password);
            // Si pasa de esta línea, es que conectó bien
            return conexion;
        } catch (SQLException e) {
            // Manejo de errores comunes
            if (e.getSQLState() != null && e.getSQLState().equals("28P01")) {
                throw new BaseDatosException("Error: La contraseña 'ContraseñaSegura123' NO es la correcta en Supabase. Necesitas resetearla en el panel web.", e);
            }
            throw new BaseDatosException("Error de conexión: " + e.getMessage(), e);
        }
    }

    public void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) { /* Ignorar */ }
        }
    }
}