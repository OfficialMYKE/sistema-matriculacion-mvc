package com.licencias.sistemalicenciasfx.dao.impl;

import com.licencias.sistemalicenciasfx.config.DatabaseConfig;
import com.licencias.sistemalicenciasfx.dao.interfaces.IUsuarioDAO;
import com.licencias.sistemalicenciasfx.model.entities.Usuario;
import com.licencias.sistemalicenciasfx.model.enums.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class UsuarioDAOImpl implements IUsuarioDAO {

    // MÉTODO LOGIN
    @Override
    public Usuario login(String username, String password) {
        Usuario usuario = null;
        String sql = "SELECT id_usuario, username, password, rol FROM usuarios WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setId(rs.getInt("id_usuario"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setPassword(rs.getString("password"));

                    String rolBD = rs.getString("rol");
                    if (rolBD != null) {
                        try {
                            usuario.setRol(Rol.valueOf(rolBD.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Rol desconocido: " + rolBD);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usuario;
    }

    // MÉTODOS OBLIGATORIOS DE IGenericDAO (Los ponemos vacíos para que no dé error)

    @Override
    public void create(Usuario entity) {
        // TODO: Implementar lógica para guardar usuario en el futuro
    }

    @Override
    public void update(Usuario entity) {
        // TODO: Implementar lógica para actualizar usuario
    }

    @Override
    public void delete(int id) {
        // TODO: Implementar lógica para borrar usuario
    }

    @Override
    public Usuario findById(int id) {
        // TODO: Implementar búsqueda por ID
        return null;
    }

    @Override
    public List<Usuario> findAll() {
        return new ArrayList<>();
    }
}