package com.licencias.sistemalicenciasfx.service;

import com.licencias.sistemalicenciasfx.dao.impl.UsuarioDAOImpl;
import com.licencias.sistemalicenciasfx.dao.interfaces.IUsuarioDAO;
import com.licencias.sistemalicenciasfx.model.entities.Usuario;
import com.licencias.sistemalicenciasfx.util.Sesion;

public class AuthService {

    // Usamos la interfaz para mantener el bajo acoplamiento
    private final IUsuarioDAO usuarioDAO;

    public AuthService() {
        // Aquí instanciamos la implementación que conecta a PostgreSQL
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    /**
     * Valida las credenciales contra la base de datos.
     * Si son correctas, guarda el usuario en la Sesión global.
     *
     * @param username Nombre de usuario o correo.
     * @param password Contraseña.
     * @return El objeto Usuario si el login es exitoso, o null si falla.
     */
    public Usuario autenticar(String username, String password) {
        // 1. Consultar a la Base de Datos
        Usuario usuario = usuarioDAO.login(username, password);

        // 2. Lógica de Negocio: Gestión de Sesión
        if (usuario != null) {
            // Si el usuario existe, lo guardamos en la clase estática Sesion
            Sesion.setUsuarioActual(usuario);
            System.out.println("✅ Login exitoso. Sesión iniciada para: " + usuario.getUsername());
        } else {
            System.out.println("❌ Login fallido. Credenciales incorrectas para: " + username);
        }

        return usuario;
    }

    /**
     * Cierra la sesión actual eliminando los datos del usuario en memoria.
     */
    public void cerrarSesion() {
        Sesion.setUsuarioActual(null);
        System.out.println("🔒 Sesión cerrada correctamente.");
    }
}