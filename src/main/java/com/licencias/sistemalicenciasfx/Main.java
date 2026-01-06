package com.licencias.sistemalicenciasfx;

import com.formdev.flatlaf.FlatDarkLaf;
import com.licencias.sistemalicenciasfx.config.DatabaseConfig;
// Importamos tu ventana de Login.
// Nota: Como tu clase Login está dentro del paquete view.Login, la importación es así:
import com.licencias.sistemalicenciasfx.view.Login;

import javax.swing.*;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {

        // 1. ACTIVAR DISEÑO MODERNO (FLATLAF)
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("⚠ No se pudo cargar el tema oscuro. Usando el de sistema.");
        }

        // 2. PRUEBA DE CONEXIÓN A BASE DE DATOS (SUPABASE)
        System.out.println("--- INICIANDO SISTEMA ---");
        System.out.println("📡 Conectando a Supabase...");

        // Usamos tu Singleton DatabaseConfig
        try (Connection conn = DatabaseConfig.getInstance().obtenerConexion()) {
            if (conn != null) {
                System.out.println("✅ ¡CONEXIÓN EXITOSA! Base de datos lista.");
            } else {
                System.err.println("❌ Error: La conexión es nula.");
            }
        } catch (Exception e) {
            System.err.println("❌ FALLO CRÍTICO DE CONEXIÓN: " + e.getMessage());
            // Si falla la BD, mostramos aviso pero intentamos abrir la app igual
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la Base de Datos.\nRevisa tu internet o contraseña.",
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        }

        // 3. ABRIR VENTANA DE LOGIN
        SwingUtilities.invokeLater(() -> {
            try {
                Login loginFrame = new Login();
                loginFrame.setVisible(true); // ¡Importante para que se vea!
                System.out.println("🖥 Ventana de Login abierta.");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ Error al abrir la ventana de Login.");
            }
        });
    }
}