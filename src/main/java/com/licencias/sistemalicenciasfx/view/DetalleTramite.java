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

public class DetalleTramite extends JFrame {
    private JPanel panelPrincipal;
    private JLabel lblFoto, lblNombre, lblCedula, lblEstado;
    private JCheckBox chkCert, chkPago, chkMultas;
    private JTextField txtTeo, txtPrac;
    private JButton btnSaveReq, btnSaveEx, btnLicencia, btnRegresar;

    private final SupabaseService service;
    private final String cedulaActual;

    // BANDERA DE CONTROL (Para evitar ventana fantasma)
    private boolean inicializacionExitosa = false;

    // COLORES
    private final Color COLOR_ACCENT = new Color(30, 58, 138); // Azul Fuerte
    private final Color COLOR_SUCCESS = new Color(40, 167, 69); // Verde
    private final Color COLOR_INPUT_BG = new Color(250, 250, 250); // Casi blanco
    private final Color COLOR_BORDER = new Color(200, 200, 200);

    public DetalleTramite(String cedula) {
        this.service = new SupabaseService();
        this.cedulaActual = cedula;

        // 1. VERIFICACIÓN INICIAL
        if (!validarUsuarioExiste()) {
            this.inicializacionExitosa = false;
            this.dispose();
            return; // Detenemos el constructor, pero el objeto sigue existiendo en memoria
        }

        // Si pasa la validación, marcamos como exitosa
        this.inicializacionExitosa = true;

        setContentPane(panelPrincipal);
        setTitle("Detalle de Trámite - Gestión");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        personalizarUI();
        cargarDatos();
        iniciarLogica();
    }

    // --- SOLUCIÓN AL ERROR DE VENTANA VACÍA ---
    @Override
    public void setVisible(boolean b) {
        // Si la inicialización falló (usuario no existe), bloqueamos que la ventana se muestre
        if (b && !inicializacionExitosa) {
            super.dispose(); // Aseguramos que se destruya
            return; // No llamamos a super.setVisible(b)
        }
        super.setVisible(b);
    }
    // ------------------------------------------

    private boolean validarUsuarioExiste() {
        boolean existe = service.obtenerTodosLosTramites().stream()
                .anyMatch(s -> s.getCedula().equals(cedulaActual));

        if (!existe) {
            JOptionPane.showMessageDialog(null,
                    "⚠️ USUARIO INEXISTENTE\n\nNo se encontró ningún trámite asociado a la cédula: " + cedulaActual + "\nPor favor verifique e intente nuevamente.",
                    "Error de Búsqueda",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void personalizarUI() {
        estilizarInput(txtTeo);
        estilizarInput(txtPrac);

        if (lblFoto != null) {
            lblFoto.setOpaque(true);
            lblFoto.setBorder(new LineBorder(COLOR_BORDER, 1));
            pintarPlaceholderFoto();
        }

        Font chkFont = new Font("Segoe UI", Font.PLAIN, 16);
        for(JCheckBox chk : new JCheckBox[]{chkCert, chkPago, chkMultas}) {
            if(chk != null) {
                chk.setBackground(Color.WHITE);
                chk.setFont(chkFont);
                chk.setFocusPainted(false);
            }
        }

        estilizarBoton(btnSaveReq, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnSaveEx, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnLicencia, COLOR_SUCCESS, Color.WHITE);

        // BOTÓN REGRESAR (Estilo Unificado)
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        if (btnRegresar != null) btnRegresar.setBorder(new LineBorder(COLOR_BORDER, 1));
    }

    private void estilizarInput(JTextField campo) {
        if(campo == null) return;
        campo.setOpaque(true);
        campo.setBackground(COLOR_INPUT_BG);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(5, 12, 5, 12)));

        campo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                campo.setBackground(Color.WHITE);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_ACCENT, 2), new EmptyBorder(4, 11, 4, 11)));
            }
            public void focusLost(FocusEvent e) {
                campo.setBackground(COLOR_INPUT_BG);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(5, 12, 5, 12)));
            }
        });
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        if(btn == null) return;
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setFocusPainted(false);
        // Si tiene borde manual (como regresar), no pintamos el default
        if (btn.getBorder() == null) btn.setBorderPainted(false);

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.putClientProperty("bgColor", bg);

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (c.isEnabled()) {
                    Color colorActual = (Color) c.getClientProperty("bgColor");
                    g2.setColor(colorActual != null ? colorActual : bg);
                } else {
                    g2.setColor(new Color(200, 200, 200));
                }
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);
                g2.dispose();

                paintTextManual(g, c, ((AbstractButton)c).getText());
            }

            private void paintTextManual(Graphics g, JComponent c, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(c.getForeground());

                g2.setFont(c.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    if (!bg.equals(Color.WHITE)) btn.putClientProperty("bgColor", bg.darker());
                    else btn.putClientProperty("bgColor", new Color(240, 240, 240));
                    btn.repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
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
        g2.setPaint(new GradientPaint(0, 0, new Color(245, 245, 245), 0, 400, new Color(225, 225, 225)));
        g2.fillRect(0, 0, 300, 400);
        g2.setColor(new Color(200, 200, 200));
        g2.fillOval(100, 100, 100, 100);
        g2.fillArc(50, 220, 200, 180, 0, 180);
        g2.dispose();
        if(lblFoto != null) lblFoto.setIcon(new ImageIcon(img));
    }

    private void cargarDatos() {
        new Thread(() -> {
            Solicitante s = service.obtenerTodosLosTramites().stream()
                    .filter(post -> post.getCedula().equals(cedulaActual))
                    .findFirst().orElse(null);
            SwingUtilities.invokeLater(() -> {
                if (s != null) {
                    if(lblNombre != null) lblNombre.setText(s.getNombreCompleto());
                    if(lblCedula != null) lblCedula.setText("CI: " + s.getCedula());
                    if(lblEstado != null) lblEstado.setText("ESTADO: " + s.getEstado().toUpperCase());

                    boolean habilitar = s.getEstado().equals("APROBADO") || s.getEstado().equals("LICENCIA_EMITIDA");
                    if(btnLicencia != null) btnLicencia.setEnabled(habilitar);

                    cargarImagen(s.getFotoUrl());
                }
            });
        }).start();
    }

    private void cargarImagen(String url) {
        if(url == null || url.isEmpty()) { pintarPlaceholderFoto(); return; }
        new Thread(() -> {
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                Image dimg = img.getScaledInstance(320, 420, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> { if(lblFoto != null) lblFoto.setIcon(new ImageIcon(dimg)); });
            } catch (Exception e) { pintarPlaceholderFoto(); }
        }).start();
    }

    private void iniciarLogica() {
        if(btnRegresar != null) btnRegresar.addActionListener(e -> dispose());

        if(btnSaveReq != null) btnSaveReq.addActionListener(e -> {
            if (chkCert.isSelected() && chkPago.isSelected() && chkMultas.isSelected()) {
                service.actualizarEstadoSolicitante(cedulaActual, "EN_EXAMENES", "Requisitos Validados");
                JOptionPane.showMessageDialog(this, "Requisitos Guardados Correctamente.");
                cargarDatos();
            } else JOptionPane.showMessageDialog(this, "Debe validar todos los requisitos.");
        });

        if(btnSaveEx != null) btnSaveEx.addActionListener(e -> {
            try {
                double t = Double.parseDouble(txtTeo.getText());
                double p = Double.parseDouble(txtPrac.getText());
                String res = (t >= 14 && p >= 14) ? "APROBADO" : "REPROBADO";
                service.registrarResultadosExamenes(cedulaActual, t, p, res);
                JOptionPane.showMessageDialog(this, "Notas Registradas. Estado Final: " + res);
                cargarDatos();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos (0-20)."); }
        });

        if(btnLicencia != null) btnLicencia.addActionListener(e -> {
            new GenerarLicencia(cedulaActual).setVisible(true);
        });
    }
}