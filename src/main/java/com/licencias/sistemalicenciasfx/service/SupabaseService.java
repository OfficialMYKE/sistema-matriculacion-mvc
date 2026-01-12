package com.licencias.sistemalicenciasfx.service;

import com.licencias.sistemalicenciasfx.config.DatabaseConfig;
import com.licencias.sistemalicenciasfx.model.entities.Solicitante;
import com.licencias.sistemalicenciasfx.model.entities.Usuario; // Importación de Usuario
import com.licencias.sistemalicenciasfx.model.exceptions.BaseDatosException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SupabaseService {

    // CONFIGURACIÓN SUPABASE
    private static final String PROJECT_URL = "https://sbxndvnhvwdppcgomkda.supabase.co";
    // Nota: Por seguridad, en producción esta clave no debería estar hardcodeada aquí.
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNieG5kdm5odndkcHBjZ29ta2RhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc3MTY2MzIsImV4cCI6MjA4MzI5MjYzMn0.W8P3KH6M5oTI-UwoG5xkCjRdefMo8iatxhbfxg9tOOo";
    private static final String BUCKET_NAME = "fotos_solicitantes";

    // ==========================================
    // GESTIÓN SOLICITANTES
    // ==========================================

    public boolean guardarSolicitante(String cedula, String nombres, String apellidos,
                                      String email, String celular, String dir,
                                      String tipo,
                                      String tipoSangre, boolean esDonante,
                                      LocalDate fechaNacimiento, String fotoUrl,
                                      String password, boolean estadoActivo) {

        String sql = "INSERT INTO solicitantes (cedula, nombres, apellidos, email, celular, direccion, " +
                "tipo_licencia, tipo_sangre, es_donante, fecha_nacimiento, foto_url, estado, fecha_registro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cedula);
            pstmt.setString(2, nombres);
            pstmt.setString(3, apellidos);
            pstmt.setString(4, email);
            pstmt.setString(5, celular);
            pstmt.setString(6, dir);
            pstmt.setString(7, tipo);
            pstmt.setString(8, tipoSangre);
            pstmt.setBoolean(9, esDonante);
            pstmt.setDate(10, Date.valueOf(fechaNacimiento));
            pstmt.setString(11, fotoUrl);
            pstmt.setString(12, estadoActivo ? "PENDIENTE" : "INACTIVO");

            return pstmt.executeUpdate() > 0;

        } catch (BaseDatosException | SQLException e) {
            System.err.println("Error al guardar solicitante: " + e.getMessage());
            return false;
        }
    }

    public Solicitante obtenerSiguientePendiente() {
        String sql = "SELECT * FROM solicitantes WHERE estado = 'PENDIENTE' ORDER BY fecha_registro ASC LIMIT 1";
        return ejecutarConsultaUnica(sql);
    }

    public Solicitante obtenerSiguienteParaExamen() {
        String sql = "SELECT * FROM solicitantes WHERE estado = 'EN_EXAMENES' ORDER BY fecha_registro ASC LIMIT 1";
        return ejecutarConsultaUnica(sql);
    }

    public List<Solicitante> obtenerTodosLosTramites() {
        List<Solicitante> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitantes ORDER BY fecha_registro DESC";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToSolicitante(rs));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener todos los trámites: " + e.getMessage());
        }
        return lista;
    }

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

    public Solicitante buscarPostulanteParaExamen(String filtro) {
        String sql = """
            SELECT *
            FROM solicitantes
            WHERE
                estado NOT IN ('LICENCIA_EMITIDA')
            AND (
                cedula ILIKE ?
                OR nombres ILIKE ?
                OR apellidos ILIKE ?
            )
            ORDER BY fecha_registro ASC
            LIMIT 1
        """;

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String patron = "%" + filtro + "%";
            pstmt.setString(1, patron);
            pstmt.setString(2, patron);
            pstmt.setString(3, patron);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSolicitante(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("Error buscarPostulanteParaExamen: " + e.getMessage());
        }
        return null;
    }

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

    public boolean registrarResultadosExamenes(String cedula, double notaTeo, double notaPrac, String estadoFinal) {
        // Actualizamos las columnas nota_teorica y nota_practica directamente en la tabla solicitantes
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

    // ==========================================
    // GESTIÓN USUARIOS (ADMIN)
    // ==========================================

    public boolean guardarUsuario(String cedula, String nom, String ape, String user, String pass, String rol, String email) {
        String sql = "INSERT INTO usuarios (cedula, nombres, apellidos, username, password, rol, email, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVO')";
        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cedula);
            pstmt.setString(2, nom);
            pstmt.setString(3, ape);
            pstmt.setString(4, user);
            pstmt.setString(5, pass);
            pstmt.setString(6, rol);
            pstmt.setString(7, email);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error guardar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarUsuario(String cedula, String nom, String ape, String user, String pass, String rol, String email) {
        String sql = "UPDATE usuarios SET nombres=?, apellidos=?, username=?, rol=?, email=? " +
                (pass.isEmpty() ? "" : ", password=?") + " WHERE cedula=?";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int i = 1;
            pstmt.setString(i++, nom);
            pstmt.setString(i++, ape);
            pstmt.setString(i++, user);
            pstmt.setString(i++, rol);
            pstmt.setString(i++, email);

            if (!pass.isEmpty()) {
                pstmt.setString(i++, pass);
            }
            pstmt.setString(i++, cedula);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cambiarEstadoUsuario(String cedula, String nuevoEstado) {
        String sql = "UPDATE usuarios SET estado=? WHERE cedula=?";
        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setString(2, cedula);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Usuario> buscarUsuarios(String filtro) {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE cedula ILIKE ? OR nombres ILIKE ? OR apellidos ILIKE ? ORDER BY id ASC";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String f = "%" + filtro + "%";
            pstmt.setString(1, f);
            pstmt.setString(2, f);
            pstmt.setString(3, f);

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                lista.add(new Usuario(
                        rs.getLong("id"),
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("rol"),
                        rs.getString("estado"),
                        rs.getString("email")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // ==========================================
    // UTILIDADES / HELPERS
    // ==========================================

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

    private Solicitante ejecutarConsultaUnica(String sql) {
        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return mapResultSetToSolicitante(rs);
            }
        } catch (Exception e) {
            System.err.println("Error en consulta única: " + e.getMessage());
        }
        return null;
    }

    // --- AQUÍ ESTÁ EL CAMBIO IMPORTANTE ---
    // Este método convierte la fila de la BD al objeto Java y AHORA incluye las notas.
    private Solicitante mapResultSetToSolicitante(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("fecha_nacimiento");
        LocalDate fechaNac = (sqlDate != null) ? sqlDate.toLocalDate() : LocalDate.of(2000, 1, 1);

        String tipoSangre = rs.getString("tipo_sangre");
        if (tipoSangre == null) tipoSangre = "O+";

        boolean esDonante = rs.getBoolean("es_donante");

        // 1. Crear el objeto base
        Solicitante s = new Solicitante(
                rs.getString("cedula"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("email"),
                rs.getString("celular"),
                rs.getString("direccion"),
                rs.getString("tipo_licencia"),
                fechaNac,
                rs.getString("foto_url"),
                rs.getString("estado"),
                tipoSangre,
                esDonante
        );

        // 2. RECUPERAR LAS NOTAS DE LA BASE DE DATOS
        // Si el valor en la BD es NULL, getDouble devuelve 0.0, lo cual es correcto.
        // Asegúrate de que en tu BD las columnas se llamen 'nota_teorica' y 'nota_practica'
        try {
            s.setNotaTeorica(rs.getDouble("nota_teorica"));
            s.setNotaPractica(rs.getDouble("nota_practica"));
        } catch (SQLException e) {
            // Si la columna no existe en la BD, no rompemos el programa, solo avisamos
            System.err.println("Advertencia: No se pudieron leer las columnas de notas (quizás no existen en la tabla).");
        }

        return s;
    }
}