package com.licencias.sistemalicenciasfx.service;

import com.licencias.sistemalicenciasfx.config.DatabaseConfig;
import com.licencias.sistemalicenciasfx.model.entities.Solicitante;
import com.licencias.sistemalicenciasfx.model.exceptions.BaseDatosException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SupabaseService {

    // --- CONFIGURACIÓN SUPABASE ---
    private static final String PROJECT_URL = "https://sbxndvnhvwdppcgomkda.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNieG5kdm5odndkcHBjZ29ta2RhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc3MTY2MzIsImV4cCI6MjA4MzI5MjYzMn0.W8P3KH6M5oTI-UwoG5xkCjRdefMo8iatxhbfxg9tOOo";
    private static final String BUCKET_NAME = "fotos_solicitantes";

    // --- 1. GUARDAR NUEVO SOLICITANTE ---
    public boolean guardarSolicitante(String cedula, String nombres, String apellidos,
                                      String email, String celular, String dir,
                                      String tipo, LocalDate fechaNacimiento, String fotoUrl) {

        String sql = "INSERT INTO solicitantes (cedula, nombres, apellidos, email, celular, direccion, tipo_licencia, fecha_registro, fecha_nacimiento, foto_url, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";

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
            pstmt.setDate(9, Date.valueOf(fechaNacimiento));
            pstmt.setString(10, fotoUrl);

            return pstmt.executeUpdate() > 0;

        } catch (BaseDatosException | SQLException e) {
            System.err.println("Error al guardar solicitante: " + e.getMessage());
            return false;
        }
    }

    // --- 2. OBTENER SIGUIENTE PENDIENTE (FASE DOCUMENTAL) ---
    public Solicitante obtenerSiguientePendiente() {
        String sql = "SELECT * FROM solicitantes WHERE estado = 'PENDIENTE' ORDER BY fecha_registro ASC LIMIT 1";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return mapResultSetToSolicitante(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- 3. OBTENER SIGUIENTE PARA EXAMEN (FASE TEÓRICO/PRÁCTICO) ---
    public Solicitante obtenerSiguienteParaExamen() {
        String sql = "SELECT * FROM solicitantes WHERE estado = 'EN_EXAMENES' ORDER BY fecha_registro ASC LIMIT 1";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return mapResultSetToSolicitante(rs);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener postulante para examen: " + e.getMessage());
        }
        return null;
    }

    // --- 4. BUSCAR SOLICITANTES POR FILTRO ---
    public List<Solicitante> buscarPendientes(String filtro) {
        List<Solicitante> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitantes WHERE estado = 'PENDIENTE' AND (cedula ILIKE ? OR apellidos ILIKE ? OR nombres ILIKE ?)";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String patron = "%" + filtro + "%";
            pstmt.setString(1, patron);
            pstmt.setString(2, patron);
            pstmt.setString(3, patron);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToSolicitante(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // --- 5. ACTUALIZAR ESTADO (PARA VERIFICACIÓN) ---
    public boolean actualizarEstadoSolicitante(String cedula, String nuevoEstado, String observaciones) {
        String sql = "UPDATE solicitantes SET estado = ?, observaciones = ? WHERE cedula = ?";
        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setString(2, observaciones);
            pstmt.setString(3, cedula);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // --- 6. REGISTRAR NOTAS Y ESTADO FINAL DE EXÁMENES ---
    public boolean registrarResultadosExamenes(String cedula, double notaTeo, double notaPrac, String estadoFinal) {
        String sql = "UPDATE solicitantes SET nota_teorica = ?, nota_practica = ?, estado = ?, observaciones = ? WHERE cedula = ?";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, notaTeo);
            pstmt.setDouble(2, notaPrac);
            pstmt.setString(3, estadoFinal);
            pstmt.setString(4, "Notas registradas - Teoría: " + notaTeo + " | Práctica: " + notaPrac);
            pstmt.setString(5, cedula);

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error al registrar exámenes: " + e.getMessage());
            return false;
        }
    }

    // --- 7. SUBIR IMAGEN A STORAGE ---
    public String subirImagen(File archivo, String cedula) {
        if (archivo == null) return null;
        try {
            String fileName = cedula + "_" + System.currentTimeMillis() + ".jpg";
            String uploadUrl = PROJECT_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .header("Content-Type", "image/jpeg")
                    .POST(HttpRequest.BodyPublishers.ofFile(archivo.toPath())).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return PROJECT_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- HELPER: MAPEO DE RESULT SET A OBJETO SOLICITANTE ---
    private Solicitante mapResultSetToSolicitante(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("fecha_nacimiento");
        LocalDate fechaNac = (sqlDate != null) ? sqlDate.toLocalDate() : LocalDate.of(2000, 1, 1);

        return new Solicitante(
                rs.getString("cedula"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("email"),
                rs.getString("celular"),
                rs.getString("direccion"),
                rs.getString("tipo_licencia"),
                fechaNac,
                rs.getString("foto_url"),
                rs.getString("estado")
        );
    }
}