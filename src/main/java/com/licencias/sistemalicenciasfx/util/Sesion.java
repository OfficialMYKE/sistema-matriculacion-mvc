package com.licencias.sistemalicenciasfx.util;

import com.licencias.sistemalicenciasfx.model.entities.Usuario;

/**
 * Clase estática para guardar los datos del usuario que inició sesión.
 * Funciona como una "Memoria Global" mientras la app está abierta.
 */
public class Sesion {

    private static Usuario usuarioActual;

    // Método que te faltaba (Setter)
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    // Método para obtener el usuario desde otras ventanas (Getter)
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // Método para cerrar sesión
    public static void cerrarSesion() {
        usuarioActual = null;
    }
}