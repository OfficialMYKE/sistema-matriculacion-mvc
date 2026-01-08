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

    // COLORES
    private final Color COLOR_ACCENT = new Color(30, 58, 138); // Azul Fuerte
    private final Color COLOR_SUCCESS = new Color(40, 167, 69); // Verde
    private final Color COLOR_INPUT_BG = new Color(250, 250, 250); // Casi blanco
    private final Color COLOR_BORDER = new Color(200, 200, 200);

    public DetalleTramite(String cedula) {
        this.service = new SupabaseService();
        this.cedulaActual = cedula;

        setContentPane(panelPrincipal);
        setTitle("Detalle de Trámite - Gestión");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        personalizarUI();
        cargarDatos();
        iniciarLogica();
    }

    private void personalizarUI() {
        estilizarInput(txtTeo);
        estilizarInput(txtPrac);

        // Foto con borde suave
        lblFoto.setOpaque(true);
        lblFoto.setBorder(new LineBorder(COLOR_BORDER, 1));
        pintarPlaceholderFoto();

        // Checkboxes: Fuente más grande y fondo blanco
        Font chkFont = new Font("Segoe UI", Font.PLAIN, 16);
        for(JCheckBox chk : new JCheckBox[]{chkCert, chkPago, chkMultas}) {
            chk.setBackground(Color.WHITE);
            chk.setFont(chkFont);
            chk.setFocusPainted(false);
        }

        // Botones: Grandes y estéticos
        estilizarBoton(btnSaveReq, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnSaveEx, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnLicencia, COLOR_SUCCESS, Color.WHITE);

        // Botón Regresar: Borde gris, texto oscuro
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER, 1));
    }

    private void estilizarInput(JTextField campo) {
        campo.setOpaque(true);
        campo.setBackground(COLOR_INPUT_BG);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Letra más grande dentro del input
        campo.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(5, 12, 5, 12)
        ));

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
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente legible
        btn.setFocusPainted(false);
        // Si el fondo es blanco, ponemos borde, si es color, quitamos borde
        btn.setBorderPainted(bg.equals(Color.WHITE));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15); // Bordes redondeados sutiles
                g2.dispose();
                super.paint(g, c);
            }
            @Override
            protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(b.getForeground());
                g2.setFont(b.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = textRect.x + (textRect.width - fm.stringWidth(text)) / 2;
                int y = textRect.y + fm.getAscent();
                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); btn.setForeground(fg); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); btn.setForeground(fg); }
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
        lblFoto.setIcon(new ImageIcon(img));
    }

    private void cargarDatos() {
        new Thread(() -> {
            Solicitante s = service.obtenerTodosLosTramites().stream()
                    .filter(post -> post.getCedula().equals(cedulaActual))
                    .findFirst().orElse(null);
            SwingUtilities.invokeLater(() -> {
                if (s != null) {
                    lblNombre.setText(s.getNombreCompleto());
                    lblCedula.setText("CI: " + s.getCedula());
                    lblEstado.setText("ESTADO: " + s.getEstado().toUpperCase());
                    btnLicencia.setEnabled(s.getEstado().equals("APROBADO"));
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
                SwingUtilities.invokeLater(() -> lblFoto.setIcon(new ImageIcon(dimg)));
            } catch (Exception e) { pintarPlaceholderFoto(); }
        }).start();
    }

    private void iniciarLogica() {
        btnRegresar.addActionListener(e -> dispose());

        btnSaveReq.addActionListener(e -> {
            if (chkCert.isSelected() && chkPago.isSelected() && chkMultas.isSelected()) {
                service.actualizarEstadoSolicitante(cedulaActual, "EN_EXAMENES", "Requisitos Validados");
                JOptionPane.showMessageDialog(this, "Requisitos Guardados Correctamente.");
                cargarDatos();
            } else JOptionPane.showMessageDialog(this, "Debe validar todos los requisitos.");
        });

        btnSaveEx.addActionListener(e -> {
            try {
                double t = Double.parseDouble(txtTeo.getText());
                double p = Double.parseDouble(txtPrac.getText());
                String res = (t >= 14 && p >= 14) ? "APROBADO" : "REPROBADO";
                service.registrarResultadosExamenes(cedulaActual, t, p, res);
                JOptionPane.showMessageDialog(this, "Notas Registradas. Estado Final: " + res);
                cargarDatos();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos (0-20)."); }
        });

        // NAVEGACIÓN A GENERAR LICENCIA
        btnLicencia.addActionListener(e -> {
            new GenerarLicencia(cedulaActual).setVisible(true);
            // Opcional: Cerrar esta ventana para limpiar el flujo
            // this.dispose();
        });
    }
}