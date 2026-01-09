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

    // MÉTODO LOGIN
    @Override
    public Usuario login(String username, String password) {
        Usuario usuario = null;

        // CORRECCIÓN 1: Seleccionamos todos los campos necesarios.
        // Asumimos que la columna en BD se llama "id", si se llama "id_usuario", cámbialo aquí.
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();

                    // CORRECCIÓN 2: Usamos getLong porque el ID ahora es Long (BIGINT)
                    usuario.setId(rs.getLong("id"));

                    usuario.setCedula(rs.getString("cedula"));
                    usuario.setNombres(rs.getString("nombres"));
                    usuario.setApellidos(rs.getString("apellidos"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setPassword(rs.getString("password"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setEstado(rs.getString("estado"));

                    // CORRECCIÓN 3: El Rol ahora es String directo, sin Enums
                    usuario.setRol(rs.getString("rol"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usuario;
    }

    // MÉTODOS DE IGenericDAO
    // Nota: Si tu interfaz obliga a usar 'int', cambia 'Long id' por 'int id'
    // pero recuerda que la Entidad Usuario usa Long.

    @Override
    public void create(Usuario entity) {
        // Por implementar
    }

    @Override
    public void update(Usuario entity) {
        // Por implementar
    }

    @Override
    public void delete(int id) {
        // Por implementar
    }

    @Override
    public Usuario findById(int id) {
        // Por implementar
        return null;
    }

    @Override
    public List<Usuario> findAll() {
        return new ArrayList<>();
    }
}