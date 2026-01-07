package com.licencias.sistemalicenciasfx.service;

import com.licencias.sistemalicenciasfx.config.DatabaseConfig;
import com.licencias.sistemalicenciasfx.model.exceptions.BaseDatosException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class SupabaseService {

    // CONFIGURACIÓN PARA STORAGE (FOTOS) - API REST
    private static final String PROJECT_URL = "https://sbxndvnhvwdppcgomkda.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNieG5kdm5odndkcHBjZ29ta2RhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc3MTY2MzIsImV4cCI6MjA4MzI5MjYzMn0.W8P3KH6M5oTI-UwoG5xkCjRdefMo8iatxhbfxg9tOOo";
    private static final String BUCKET_NAME = "fotos_solicitantes";

    // GUARDAR DATOS (Usando tu DatabaseConfig)
    public boolean guardarSolicitante(String cedula, String nombres, String apellidos,
                                      String email, String celular, String dir,
                                      String tipo, String fotoUrl) {

        String sql = "INSERT INTO solicitantes (cedula, nombres, apellidos, email, celular, direccion, tipo_licencia, fecha_registro, foto_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Usamos try-with-resources para asegurar que la conexión se cierre
        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cedula);
            pstmt.setString(2, nombres);
            pstmt.setString(3, apellidos);
            pstmt.setString(4, email);
            pstmt.setString(5, celular);
            pstmt.setString(6, dir);
            pstmt.setString(7, tipo);
            pstmt.setObject(8, LocalDate.now());
            pstmt.setString(9, fotoUrl); // Puede ser null si no hay foto

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (BaseDatosException | SQLException e) {
            System.err.println(" Error al guardar en base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // SUBIR IMAGEN (API HTTP Nativa de Java)
    public String subirImagen(File archivo, String cedula) {
        if (archivo == null) return null;

        try {
            // Nombre único para evitar sobrescribir (cedula_timestamp.jpg)
            String fileName = cedula + "_" + System.currentTimeMillis() + ".jpg";

            // Endpoint exacto de Supabase Storage
            String uploadUrl = PROJECT_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + SUPABASE_KEY) // Autenticación con tu llave real
                    .header("Content-Type", "image/jpeg")
                    .POST(HttpRequest.BodyPublishers.ofFile(archivo.toPath()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Código 200 significa éxito
            if (response.statusCode() == 200) {
                // Construimos la URL pública para guardarla en la BD
                return PROJECT_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
            } else {
                System.err.println("Error Subida (" + response.statusCode() + "): " + response.body());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}