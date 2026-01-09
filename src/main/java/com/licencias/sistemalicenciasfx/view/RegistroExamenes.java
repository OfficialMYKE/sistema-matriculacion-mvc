package com.licencias.sistemalicenciasfx.view;

import com.licencias.sistemalicenciasfx.model.entities.Solicitante;
import com.licencias.sistemalicenciasfx.service.SupabaseService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class RegistroExamenes extends JFrame {

    private JPanel panelPrincipal;
    private JLabel lblFoto;
    private JLabel lblNombre;
    private JLabel lblCedula;
    private JTextField txtNotaTeorica;
    private JTextField txtNotaPractica;
    private JButton btnGuardar;
    private JButton btnRegresar;

    private final SupabaseService supabaseService;
    private Solicitante solicitanteActual;

    // CONFIGURACIÓN ESTÉTICA
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138); // Azul
    private final Color COLOR_DANGER = new Color(220, 53, 69);  // Rojo

    public RegistroExamenes() {
        this.supabaseService = new SupabaseService();
        setContentPane(panelPrincipal);
        setTitle("Registro de Exámenes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        personalizarUI();
        iniciarLogica();
    }

    private void personalizarUI() {
        // INPUTS ESTILO REGISTRO
        estilizarInput(txtNotaTeorica);
        estilizarInput(txtNotaPractica);

        // FOTO
        lblFoto.setOpaque(true);
        lblFoto.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        pintarPlaceholderFoto();

        // --- BOTONES ---

        // 1. Botón Guardar (Azul, Texto Blanco)
        estilizarBoton(btnGuardar, COLOR_ACCENT, Color.WHITE);

        // 2. Botón Regresar (Blanco, Texto Gris Oscuro, Borde Gris) - IGUAL QUE GESTIÓN USUARIOS
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
    }

    private void estilizarInput(JTextField campo) {
        campo.setOpaque(true);
        campo.setBackground(COLOR_BG_INPUT);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(10, 15, 10, 15)));

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBackground(Color.WHITE);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_ACCENT, 2), new EmptyBorder(9, 14, 9, 14)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                campo.setBackground(COLOR_BG_INPUT);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(10, 15, 10, 15)));
            }
        });
    }

    // --- MÉTODO DE ESTILO DE BOTÓN ROBUSTO (Evita duplicidad de texto) ---
    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Regular
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Guardamos propiedad para hover
        btn.putClientProperty("bgColor", bg);

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar Fondo
                if (c.isEnabled()) {
                    Color colorActual = (Color) c.getClientProperty("bgColor");
                    g2.setColor(colorActual != null ? colorActual : bg);
                } else {
                    g2.setColor(new Color(200, 200, 200));
                }
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                g2.dispose();

                // DIBUJAR TEXTO MANUALMENTE (Para control total del color y nitidez)
                paintTextManual(g, c, ((AbstractButton)c).getText());
            }

            private void paintTextManual(Graphics g, JComponent c, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Usamos el color configurado en el componente (fg)
                g2.setColor(c.getForeground());

                g2.setFont(c.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();

                // Centrar texto
                int x = (c.getWidth() - textWidth) / 2;
                int y = (c.getHeight() + textHeight) / 2 - 2;

                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if(btn.isEnabled()) {
                    // Si es blanco, oscurecemos a gris claro, si es color, oscurecemos el color
                    if (!bg.equals(Color.WHITE)) {
                        btn.putClientProperty("bgColor", bg.darker());
                    } else {
                        btn.putClientProperty("bgColor", new Color(240, 240, 240));
                    }
                    btn.repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                if(btn.isEnabled()) {
                    btn.putClientProperty("bgColor", bg);
                    btn.repaint();
                }
            }
        });
    }

    private void pintarPlaceholderFoto() {
        BufferedImage img = new BufferedImage(300, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(240, 240, 240), 0, 400, new Color(220, 220, 220)));
        g2.fillRect(0, 0, 300, 400);
        g2.setColor(new Color(180, 180, 180));
        g2.fillOval(100, 80, 100, 100);
        g2.fillArc(50, 200, 200, 180, 0, 180);
        g2.dispose();
        lblFoto.setIcon(new ImageIcon(img));
    }

    private void iniciarLogica() {
        btnRegresar.addActionListener(e -> this.dispose());
        cargarSiguientePostulante();

        btnGuardar.addActionListener(e -> {
            if(solicitanteActual == null) return;

            try {
                double teo = Double.parseDouble(txtNotaTeorica.getText());
                double prac = Double.parseDouble(txtNotaPractica.getText());

                if (teo < 0 || teo > 20 || prac < 0 || prac > 20) {
                    JOptionPane.showMessageDialog(this, "Las notas deben estar entre 0 y 20.");
                    return;
                }

                String nuevoEstado = (teo >= 14 && prac >= 14) ? "APROBADO" : "REPROBADO";
                String obs = "Nota Teoría: " + teo + " | Nota Práctica: " + prac;

                // Bloquear botón mientras guarda
                btnGuardar.setEnabled(false);
                btnGuardar.setText("Guardando...");

                new Thread(() -> {
                    if(supabaseService.actualizarEstadoSolicitante(solicitanteActual.getCedula(), nuevoEstado, obs)) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Resultados guardados. Estado: " + nuevoEstado);
                            cargarSiguientePostulante();
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Error al guardar notas.");
                            btnGuardar.setEnabled(true);
                            btnGuardar.setText("Guardar Notas");
                        });
                    }
                }).start();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos para las notas.");
            }
        });
    }

    private void cargarSiguientePostulante() {
        new Thread(() -> {
            this.solicitanteActual = supabaseService.obtenerSiguienteParaExamen();
            SwingUtilities.invokeLater(() -> {
                btnGuardar.setText("Guardar Notas");
                if (solicitanteActual != null) {
                    lblNombre.setText(solicitanteActual.getNombreCompleto());
                    lblCedula.setText("CI: " + solicitanteActual.getCedula());
                    cargarImagen(solicitanteActual.getFotoUrl());
                    btnGuardar.setEnabled(true);
                } else {
                    lblNombre.setText("No hay postulantes para examen");
                    lblCedula.setText("---");
                    pintarPlaceholderFoto();
                    btnGuardar.setEnabled(false);
                }
                txtNotaTeorica.setText(""); txtNotaPractica.setText("");
            });
        }).start();
    }

    private void cargarImagen(String u) {
        if(u == null) { pintarPlaceholderFoto(); return; }
        new Thread(() -> {
            try {
                BufferedImage i = ImageIO.read(new URL(u));
                if(i != null) {
                    Image d = i.getScaledInstance(300, 400, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> lblFoto.setIcon(new ImageIcon(d)));
                }
            } catch (Exception e) { pintarPlaceholderFoto(); }
        }).start();
    }
}