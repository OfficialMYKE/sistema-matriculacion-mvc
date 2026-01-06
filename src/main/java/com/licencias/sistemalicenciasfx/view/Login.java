package com.licencias.sistemalicenciasfx.view;

import javax.swing.*;

public class Login extends JFrame {
    private JPanel mainPanel; // El panel que creaste en el Designer (.form)

    public Login() {
        setTitle("Login - Sistema Licencias");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla

        // Si ya usaste el Designer, IntelliJ inyectará el código aquí
        // Si no has diseñado nada, saldrá una ventana vacía pequeña, pero no dará error.
    }
}