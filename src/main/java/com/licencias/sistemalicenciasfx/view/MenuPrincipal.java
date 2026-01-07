package com.licencias.sistemalicenciasfx.view;

import com.licencias.sistemalicenciasfx.model.entities.Usuario;
import com.licencias.sistemalicenciasfx.model.enums.Rol;
import com.licencias.sistemalicenciasfx.util.Sesion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class MenuPrincipal extends JFrame {

    private JPanel panelPrincipal;
    private JPanel panelMenu;
    private JPanel panelContenido;
    private JLabel lblTitulo;

    private final Usuario usuarioActual;

    // --- COLORES ---
    private final Color COLOR_AZUL_FONDO = new Color(30, 58, 138); // #1E3A8A
    private final Color COLOR_AZUL_HOVER = new Color(255, 255, 255, 25);
    private final Color COLOR_ROJO_PRO = new Color(198, 40, 40); // Rojo Carmesí
    private final Color COLOR_ROJO_HOVER = new Color(183, 28, 28);
    private final Color COLOR_BLANCO = Color.WHITE;
    private final Color COLOR_TEXTO_SECUNDARIO = new Color(229, 231, 235);

    public MenuPrincipal(Usuario usuario) {
        this.usuarioActual = usuario;

        setContentPane(panelPrincipal);
        setTitle("Sistema de Gestión de Licencias - EPN");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        construirMenuLateral();
    }

    private void construirMenuLateral() {
        if(panelMenu == null) return;
        panelMenu.removeAll();

        panelMenu.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // 1. HEADER
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(15, 0, 0, 0);

        JLabel lblLogo = new JLabel("EPN");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 52));
        lblLogo.setForeground(COLOR_BLANCO);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        panelMenu.add(lblLogo, gbc);

        gbc.gridy++;
        JLabel lblSub = new JLabel("GESTIÓN LICENCIAS");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        panelMenu.add(lblSub, gbc);

        gbc.gridy++;
        panelMenu.add(Box.createVerticalStrut(40), gbc);

        // 2. BOTONES
        gbc.gridy++; agregarSeparador("OPERACIONES", gbc);

        // --- AQUÍ ESTÁ EL CAMBIO PARA ABRIR LA VENTANA ---
        gbc.gridy++;
        agregarBoton("Registrar Solicitante", IconType.USER_ADD, e -> {
            navegar("Registrar Solicitante");
            // Abre la ventana de Registro
            new RegistroSolicitante().setVisible(true);
        }, gbc);

        // Los demás botones siguen igual por ahora (navegación simulada)
        gbc.gridy++; agregarBoton("Verificar Requisitos", IconType.CHECK, e -> navegar("Verificar Requisitos"), gbc);
        gbc.gridy++; agregarBoton("Registrar Exámenes", IconType.DOC_EDIT, e -> navegar("Registrar Exámenes"), gbc);
        gbc.gridy++; agregarBoton("Gestión de Trámites", IconType.FOLDER, e -> navegar("Gestión de Trámites"), gbc);
        gbc.gridy++; agregarBoton("Generar Licencia", IconType.CARD, e -> navegar("Generar Licencia"), gbc);

        if (usuarioActual.getRol() == Rol.ADMINISTRADOR) {
            gbc.gridy++;
            panelMenu.add(Box.createVerticalStrut(15), gbc);

            gbc.gridy++; agregarSeparador("ADMINISTRACIÓN", gbc);

            gbc.gridy++; agregarBoton("Gestión de Usuarios", IconType.GROUP, e -> navegar("Gestión de Usuarios"), gbc);
            gbc.gridy++; agregarBoton("Reportes y Estadísticas", IconType.CHART, e -> navegar("Reportes"), gbc);
        }

        // 3. FOOTER
        gbc.gridy++;
        gbc.weighty = 1.0;
        panelMenu.add(Box.createVerticalGlue(), gbc);

        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets = new Insets(10, 0, 5, 0);

        JLabel lblUser = new JLabel("Usuario: " + usuarioActual.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(COLOR_BLANCO);
        lblUser.setHorizontalAlignment(SwingConstants.CENTER);
        panelMenu.add(lblUser, gbc);

        // Botón Cerrar Sesión
        gbc.gridy++;
        gbc.insets = new Insets(15, 20, 30, 20);
        JButton btnSalir = createLogoutButton("Cerrar Sesión");
        btnSalir.addActionListener(e -> {
            Sesion.cerrarSesion();
            this.dispose();
            new Login().setVisible(true);
        });
        panelMenu.add(btnSalir, gbc);

        panelMenu.revalidate();
        panelMenu.repaint();
    }

    // --- HELPERS VISUALES ---

    private void agregarSeparador(String texto, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(255, 255, 255, 120));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        p.add(lbl);

        Insets old = gbc.insets;
        gbc.insets = new Insets(15, 0, 5, 0);
        panelMenu.add(p, gbc);
        gbc.insets = old;
    }

    private void agregarBoton(String texto, IconType iconType, java.awt.event.ActionListener accion, GridBagConstraints gbc) {
        JButton btn = new JButton(texto);
        btn.setIcon(new VectorIcon(iconType, Color.WHITE, 18));
        btn.setIconTextGap(15);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(COLOR_BLANCO);
        btn.setBackground(COLOR_AZUL_FONDO);
        btn.setPreferredSize(new Dimension(260, 42));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 20, 0, 0));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_AZUL_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setBackground(COLOR_AZUL_FONDO); }
        });

        btn.addActionListener(accion);
        panelMenu.add(btn, gbc);
    }

    private JButton createLogoutButton(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_ROJO_PRO);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setPreferredSize(new Dimension(240, 55));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_ROJO_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setBackground(COLOR_ROJO_PRO); }
        });
        return btn;
    }

    private void navegar(String titulo) {
        if(lblTitulo != null) lblTitulo.setText(titulo);
    }

    // --- CLASE INTERNA PARA DIBUJAR ICONOS ---
    private enum IconType { USER_ADD, CHECK, DOC_EDIT, FOLDER, CARD, GROUP, CHART }

    private static class VectorIcon implements Icon {
        private final IconType type;
        private final Color color;
        private final int size;

        public VectorIcon(IconType type, Color color, int size) {
            this.type = type;
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.8f));
            g2.setColor(color);
            g2.translate(x, y);

            switch (type) {
                case USER_ADD:
                    g2.drawOval(4, 2, 6, 6);
                    g2.drawArc(1, 10, 12, 8, 0, 180);
                    g2.drawLine(14, 5, 18, 5);
                    g2.drawLine(16, 3, 16, 7);
                    break;
                case CHECK:
                    g2.drawPolyline(new int[]{3, 9, 16}, new int[]{9, 14, 4}, 3);
                    break;
                case DOC_EDIT:
                    g2.drawRect(4, 3, 10, 12);
                    g2.drawLine(6, 6, 12, 6);
                    g2.drawLine(6, 9, 10, 9);
                    break;
                case FOLDER:
                    Path2D folder = new Path2D.Double();
                    folder.moveTo(2, 4); folder.lineTo(7, 4); folder.lineTo(9, 6); folder.lineTo(16, 6);
                    folder.lineTo(16, 14); folder.lineTo(2, 14); folder.closePath();
                    g2.draw(folder);
                    break;
                case CARD:
                    g2.drawRoundRect(2, 4, 16, 10, 2, 2);
                    g2.drawRect(4, 6, 4, 4);
                    g2.drawLine(10, 7, 16, 7);
                    g2.drawLine(10, 10, 14, 10);
                    break;
                case GROUP:
                    g2.drawOval(2, 3, 5, 5);
                    g2.drawArc(0, 9, 9, 6, 0, 180);
                    g2.drawOval(10, 2, 5, 5);
                    g2.drawArc(9, 8, 9, 6, 0, 180);
                    break;
                case CHART:
                    g2.drawLine(2, 16, 16, 16);
                    g2.drawLine(2, 2, 2, 16);
                    g2.drawPolyline(new int[]{2, 6, 10, 14}, new int[]{12, 8, 10, 4}, 4);
                    break;
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}