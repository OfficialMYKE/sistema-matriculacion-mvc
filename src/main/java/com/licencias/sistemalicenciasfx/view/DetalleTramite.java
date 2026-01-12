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
import java.util.Locale;

public class DetalleTramite extends JFrame {
    private JPanel panelPrincipal;
    // Asegúrate de que en el .form estos componentes tengan ESTOS NOMBRES EXACTOS:
    private JLabel lblFoto, lblNombre, lblCedula, lblEstado;
    private JCheckBox chkCert, chkPago, chkMultas;
    private JTextField txtTeo, txtPrac;
    private JButton btnSaveReq, btnSaveEx, btnLicencia, btnRegresar;

    private final SupabaseService service;
    private final String cedulaActual;
    private boolean inicializacionExitosa = false;

    // COLORES
    private final Color COLOR_ACCENT = new Color(30, 58, 138);
    private final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private final Color COLOR_INPUT_BG = new Color(250, 250, 250);
    private final Color COLOR_BORDER = new Color(200, 200, 200);

    public DetalleTramite(String cedula) {
        this.service = new SupabaseService();
        this.cedulaActual = cedula;

        System.out.println("--- ABRIENDO DETALLE INTEGRAL PARA: " + cedula + " ---");

        if (!validarUsuarioExiste()) {
            this.inicializacionExitosa = false;
            this.dispose();
            return;
        }
        this.inicializacionExitosa = true;

        setContentPane(panelPrincipal);
        setTitle("Detalle Integral del Trámite"); // Título actualizado
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        personalizarUI();
        cargarDatos();
        iniciarLogica();
    }

    @Override
    public void setVisible(boolean b) {
        if (b && !inicializacionExitosa) {
            super.dispose();
            return;
        }
        super.setVisible(b);
    }

    private boolean validarUsuarioExiste() {
        boolean existe = service.obtenerTodosLosTramites().stream()
                .anyMatch(s -> s.getCedula().equals(cedulaActual));

        if (!existe) {
            JOptionPane.showMessageDialog(null, "⚠️ No se encontró trámite para: " + cedulaActual, "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void personalizarUI() {
        // DIAGNÓSTICO: Verificamos si los componentes gráficos están conectados
        if (txtTeo == null) System.err.println("ERROR GRAVE: 'txtTeo' es NULL. Revisa el nombre en el .form");
        if (txtPrac == null) System.err.println("ERROR GRAVE: 'txtPrac' es NULL. Revisa el nombre en el .form");

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
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
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
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
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
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                if (bg.equals(Color.WHITE)) {
                    g2.setColor(COLOR_BORDER);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 20, 20);
                }
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
        // ... (código imagen igual)
        BufferedImage img = new BufferedImage(300, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(new Color(225, 225, 225));
        g2.fillRect(0, 0, 300, 400);
        g2.dispose();
        if(lblFoto != null) lblFoto.setIcon(new ImageIcon(img));
    }

    // CARGA DE DATOS (AQUÍ ESTÁ LA CLAVE)
    private void cargarDatos() {
        new Thread(() -> {
            Solicitante s = service.obtenerTodosLosTramites().stream()
                    .filter(post -> post.getCedula().equals(cedulaActual))
                    .findFirst().orElse(null);

            SwingUtilities.invokeLater(() -> {
                if (s != null) {
                    System.out.println("DEBUG: Datos encontrados para " + s.getCedula());
                    System.out.println("DEBUG: Nota T: " + s.getNotaTeorica() + " | Nota P: " + s.getNotaPractica());

                    if(lblNombre != null) lblNombre.setText(s.getNombreCompleto());
                    if(lblCedula != null) lblCedula.setText("CI: " + s.getCedula());
                    if(lblEstado != null) lblEstado.setText("ESTADO: " + s.getEstado().toUpperCase());

                    // SETEAMOS LOS TEXTOS DE LAS NOTAS CON FORMATO 0.00
                    if(txtTeo != null) {
                        txtTeo.setText(String.format(Locale.US, "%.2f", s.getNotaTeorica()));
                        System.out.println("DEBUG: Seteando txtTeo con: " + txtTeo.getText());
                    }

                    if(txtPrac != null) {
                        txtPrac.setText(String.format(Locale.US, "%.2f", s.getNotaPractica()));
                        System.out.println("DEBUG: Seteando txtPrac con: " + txtPrac.getText());
                    }

                    // Checkboxes
                    if (!s.getEstado().equalsIgnoreCase("PENDIENTE") && !s.getEstado().equalsIgnoreCase("RECHAZADO")) {
                        if(chkCert != null) chkCert.setSelected(true);
                        if(chkPago != null) chkPago.setSelected(true);
                        if(chkMultas != null) chkMultas.setSelected(true);
                    }

                    boolean habilitarLicencia = s.getEstado().equals("APROBADO") || s.getEstado().equals("LICENCIA_EMITIDA");
                    if(btnLicencia != null) btnLicencia.setEnabled(habilitarLicencia);

                    cargarImagen(s.getFotoUrl());
                } else {
                    System.err.println("DEBUG: No se encontró el solicitante en la lista.");
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
                // CORRECCIÓN: Aceptar comas y puntos
                String valT = txtTeo.getText().replace(",", ".");
                String valP = txtPrac.getText().replace(",", ".");

                double t = Double.parseDouble(valT);
                double p = Double.parseDouble(valP);

                if (t < 0 || t > 20 || p < 0 || p > 20) {
                    JOptionPane.showMessageDialog(this, "Las notas deben estar entre 0 y 20.");
                    return;
                }

                String res = (t >= 14 && p >= 14) ? "APROBADO" : "REPROBADO";
                service.registrarResultadosExamenes(cedulaActual, t, p, res);
                JOptionPane.showMessageDialog(this, "Notas Registradas. Estado Final: " + res);
                cargarDatos();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos (use punto o coma).");
            }
        });

        if(btnLicencia != null) btnLicencia.addActionListener(e -> {
            new GenerarLicencia(cedulaActual).setVisible(true);
        });
    }
}