package com.licencias.sistemalicenciasfx.config;

import com.licencias.sistemalicenciasfx.model.exceptions.BaseDatosException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    // Variable estática para el patrón Singleton
    private static DatabaseConfig instancia;

    // Variables para guardar los datos de conexión
    private final String url;
    private final String usuario;
    private final String password;

    // Constructor privado para que no se pueda hacer new DatabaseConfig() desde fuera
    private DatabaseConfig() {
        // Configuración del host de mi proyecto en Supabase
        String host = "db.sbxndvnhvwdppcgomkda.supabase.co";
        String puerto = "5432";
        String dbName = "postgres";

        // Armamos la URL de conexión.
        // Es importante el '?sslmode=require' porque Supabase lo pide por seguridad
        this.url = "jdbc:postgresql://" + host + ":" + puerto + "/" + dbName + "?sslmode=require";

        // Credenciales de la base de datos
        this.usuario = "postgres"; // Usuario por defecto
        this.password = "ContraseñaSegura123"; // Contraseña del panel de Supabase

        // Cargamos el driver de PostgreSQL
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Falta la librería de PostgreSQL.");
            e.printStackTrace();
        }
    }
<<<<<<< HEAD
=======

>>>>>>> cf90876ab15e7fa8c810fcb2659d32a84739ddd1
    // Método para obtener la instancia única (Singleton)
    public static synchronized DatabaseConfig getInstance() {
        if (instancia == null) {
            instancia = new DatabaseConfig();
        }
        return instancia;
    }
<<<<<<< HEAD
=======

>>>>>>> cf90876ab15e7fa8c810fcb2659d32a84739ddd1
    // Método para conectar a la base de datos
    public Connection obtenerConexion() throws BaseDatosException {
        try {
            // Intentamos conectar con los datos configurados
            Connection conexion = DriverManager.getConnection(url, usuario, password);
            return conexion;
        } catch (SQLException e) {
            // Verificamos si el error es por contraseña incorrecta (código 28P01)
            if (e.getSQLState() != null && e.getSQLState().equals("28P01")) {
                throw new BaseDatosException("Error: La contraseña no coincide con la de Supabase.", e);
            }
            // Cualquier otro error de conexión
            throw new BaseDatosException("No se pudo conectar: " + e.getMessage(), e);
        }
    }
<<<<<<< HEAD
=======

>>>>>>> cf90876ab15e7fa8c810fcb2659d32a84739ddd1
    // Método para cerrar la conexión y liberar recursos
    public void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                // No hacemos nada si falla al cerrar
            }
        }
    }
}


