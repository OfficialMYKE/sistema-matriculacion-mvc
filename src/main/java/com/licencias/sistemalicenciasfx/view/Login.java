package com.licencias.sistemalicenciasfx.view;

// Imports de Java Swing
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

// Imports de TU Arquitectura
import com.licencias.sistemalicenciasfx.model.entities.Usuario;
import com.licencias.sistemalicenciasfx.service.AuthService;

public class Login extends JFrame {

    // --- COMPONENTES VINCULADOS AL .FORM ---
    private JPanel panelPrincipal;
    private JPanel panelIzquierdo;
    private JPanel panelDerecho;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRol;
    private JCheckBox chkMostrarPass;
    private JButton btnIngresar;
    private JButton btnSalir;

    // --- COLORES CORPORATIVOS ---
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138); // Azul Institucional

    // --- SERVICIO DE AUTENTICACIÓN ---
    private final AuthService authService;

    public Login() {
        // Inicializamos el servicio
        this.authService = new AuthService();

        // Vincula el diseño visual (.form)
        setContentPane(panelPrincipal);

        // CONFIGURACIÓN DE VENTANA
        setTitle("EPN - Sistema de Gestión de Licencias");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa

        aplicarEstilos();
        iniciarEventos();
    }

    private void aplicarEstilos() {
        if (panelIzquierdo != null) {
            panelIzquierdo.setBackground(COLOR_ACCENT);
        }

        estilizarInputRectangular(txtUsuario);
        estilizarInputRectangular(txtPassword);

        if(cmbRol != null) cmbRol.setBackground(COLOR_BG_INPUT);

        if (btnIngresar != null) {
            btnIngresar.setForeground(Color.WHITE);
            btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnIngresar.setFocusPainted(false);
            btnIngresar.setBorderPainted(false);
            btnIngresar.setContentAreaFilled(false);
            btnIngresar.setOpaque(false);

            btnIngresar.setUI(new BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_ACCENT);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                    g2.dispose();
                    super.paint(g, c);
                }
            });
        }

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
            btnSalir.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btnSalir.setText("<html><u>Salir de la aplicación</u></html>"); }
                public void mouseExited(MouseEvent e) { btnSalir.setText("Salir de la aplicación"); }
            });
        }
    }

    private void estilizarInputRectangular(JTextField campo) {
        if (campo == null) return;
        campo.setOpaque(true);
        campo.setBackground(COLOR_BG_INPUT);
        campo.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER_INPUT, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBackground(Color.WHITE);
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
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // 1. Obtener lo que el usuario ve en el Combo
        String rolSeleccionado = (String) cmbRol.getSelectedItem();

        // -----------------------------------------------------------
        // SOLUCIÓN CLAVE: TRADUCCIÓN DE ROL VISUAL A ROL DE BASE DE DATOS
        // -----------------------------------------------------------
        if (rolSeleccionado != null) {
            if (rolSeleccionado.equalsIgnoreCase("ADMINISTRADOR")) {
                rolSeleccionado = "ADMIN"; // Convertimos lo visual a lo técnico
            }
            else if (rolSeleccionado.equalsIgnoreCase("DIGITADOR")) {
                rolSeleccionado = "DIGITADOR";
            }
        }
        // -----------------------------------------------------------

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese usuario y contraseña.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario usuarioEncontrado = authService.autenticar(usuario, password);

            if (usuarioEncontrado != null) {

                // 2. Comparar el Rol de la BD con el Rol "Traducido"
                // Usamos equalsIgnoreCase para mayor seguridad
                if (usuarioEncontrado.getRol().equalsIgnoreCase(rolSeleccionado)) {

                    // ¡LOGIN EXITOSO!
                    JOptionPane.showMessageDialog(this, "¡Bienvenido " + usuarioEncontrado.getUsername() + "!");
                    this.dispose(); // Cerrar login

                    // Abrir menú principal pasando el usuario
                    new MenuPrincipal(usuarioEncontrado).setVisible(true);

                } else {
                    JOptionPane.showMessageDialog(this,
                            "Credenciales correctas, pero el rol no coincide.\n" +
                                    "Rol en el sistema: " + usuarioEncontrado.getRol() + "\n" +
                                    "Rol seleccionado: " + rolSeleccionado,
                            "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}