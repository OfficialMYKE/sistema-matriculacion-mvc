package com.licencias.sistemalicenciasfx.view;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

public class Login extends JFrame {

    // --- COMPONENTES VINCULADOS AL .FORM ---
    private JPanel panelPrincipal;
    private JPanel panelIzquierdo;
    private JPanel panelDerecho;  // <--- ¡ESTA ERA LA VARIABLE QUE FALTABA!

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRol;
    private JCheckBox chkMostrarPass;
    private JButton btnIngresar;
    private JButton btnSalir;

    // COLORES
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138);

    public Login() {
        // Vincula el diseño visual (.form)
        setContentPane(panelPrincipal);

        // CONFIGURACIÓN DE VENTANA
        setTitle("EPN - Sistema de Gestión de Licencias");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa

        // APLICAR DISEÑO
        aplicarEstilos();
        iniciarEventos();
    }

    private void aplicarEstilos() {
        // 1. ESTILO RECTANGULAR PARA INPUTS
        estilizarInputRectangular(txtUsuario);
        estilizarInputRectangular(txtPassword);

        // ComboBox simple
        if(cmbRol != null) cmbRol.setBackground(COLOR_BG_INPUT);

        // 2. ESTILO REDONDEADO SOLO PARA EL BOTÓN INGRESAR
        if (btnIngresar != null) {
            btnIngresar.setForeground(Color.WHITE);
            btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnIngresar.setFocusPainted(false);
            btnIngresar.setBorderPainted(false);
            btnIngresar.setContentAreaFilled(false);
            btnIngresar.setOpaque(false);

            // Pintamos el botón manualmente para que sea redondo
            btnIngresar.setUI(new BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_ACCENT);
                    // Radio de 20px para el botón
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                    g2.dispose();
                    super.paint(g, c);
                }
            });
        }

        // Botón Salir
        if (btnSalir != null) {
            btnSalir.setForeground(Color.GRAY);
            btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSalir.setBorderPainted(false);
            btnSalir.setContentAreaFilled(false);
        }
    }

    private void iniciarEventos() {
        if(btnIngresar != null) {
            btnIngresar.addActionListener(e -> logearse());
            this.getRootPane().setDefaultButton(btnIngresar);
        }

        if(chkMostrarPass != null) {
            chkMostrarPass.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) txtPassword.setEchoChar((char) 0);
                else txtPassword.setEchoChar('•');
            });
        }

        if(btnSalir != null) {
            btnSalir.addActionListener(e -> {
                if(JOptionPane.showConfirmDialog(this, "¿Cerrar sistema?", "Salir", JOptionPane.YES_NO_OPTION) == 0)
                    System.exit(0);
            });
            // Efecto Hover
            btnSalir.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btnSalir.setText("<html><u>Salir de la aplicación</u></html>"); }
                public void mouseExited(MouseEvent e) { btnSalir.setText("Salir de la aplicación"); }
            });
        }
    }

    // --- ESTILO RECTANGULAR (FLAT) PARA INPUTS ---
    private void estilizarInputRectangular(JTextField campo) {
        if (campo == null) return;

        campo.setOpaque(true); // Fondo sólido
        campo.setBackground(COLOR_BG_INPUT);

        // Borde LINEAL (Rectangular) Gris
        campo.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER_INPUT, 1),
                new EmptyBorder(10, 15, 10, 15) // Relleno interno
        ));

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBackground(Color.WHITE);
                // Borde LINEAL Azul al enfocar (Grosor 2)
                campo.setBorder(new CompoundBorder(
                        new LineBorder(COLOR_ACCENT, 2),
                        new EmptyBorder(9, 14, 9, 14)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.setBackground(COLOR_BG_INPUT);
                campo.setBorder(new CompoundBorder(
                        new LineBorder(COLOR_BORDER_INPUT, 1),
                        new EmptyBorder(10, 15, 10, 15)
                ));
            }
        });
    }

    private void logearse() {
        JOptionPane.showMessageDialog(this, "Conectando...");
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}