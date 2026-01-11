package com.licencias.sistemalicenciasfx.dao.impl;

import com.licencias.sistemalicenciasfx.config.DatabaseConfig;
import com.licencias.sistemalicenciasfx.dao.interfaces.IUsuarioDAO;
import com.licencias.sistemalicenciasfx.model.entities.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

public class UsuarioDAOImpl implements IUsuarioDAO {

    // Método para validar el login del usuario
    @Override
    public Usuario login(String username, String password) {
        Usuario usuario = null;

        // Consulta SQL básica para buscar el usuario y contraseña
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Pasamos los parámetros a la consulta
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                // Si la consulta devuelve algo, es que el usuario existe
                if (rs.next()) {
                    usuario = new Usuario();

                    // Mapeamos los datos de la base al objeto Java
                    usuario.setId(rs.getLong("id")); // El ID es numérico
                    usuario.setCedula(rs.getString("cedula"));
                    usuario.setNombres(rs.getString("nombres"));
                    usuario.setApellidos(rs.getString("apellidos"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setPassword(rs.getString("password"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setEstado(rs.getString("estado"));

                    // El rol viene como texto (ADMIN, ANALISTA, etc.)
                    usuario.setRol(rs.getString("rol"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error al intentar loguearse: " + e.getMessage());
            e.printStackTrace();
        }
        return usuario;
    }

    // --- MÉTODOS CRUD PENDIENTES ---
    // Se implementarán según se vayan necesitando en las pantallas

    @Override
    public void create(Usuario entity) {
        // TODO: Implementar registro de usuarios
    }

    @Override
    public void update(Usuario entity) {
        // TODO: Implementar actualización de datos
    }

    @Override
    public void delete(int id) {
        // TODO: Implementar borrado lógico o físico
    }

    @Override
    public Usuario findById(int id) {
        // TODO: Buscar usuario por ID
        return null;
    }

    @Override
    public List<Usuario> findAll() {
        // Retorna lista vacía por ahora
        return new ArrayList<>();
    }
}