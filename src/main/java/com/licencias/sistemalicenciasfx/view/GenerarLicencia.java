package com.licencias.sistemalicenciasfx.view;

import com.licencias.sistemalicenciasfx.model.entities.Solicitante;
import com.licencias.sistemalicenciasfx.service.SupabaseService;
import com.licencias.sistemalicenciasfx.util.PDFGenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GenerarLicencia extends JFrame {
    private JPanel panelPrincipal;
    private JLabel lblFoto, lblDatosLicencia, lblBarcode;
    private JTextField txtEmision, txtVencimiento;
    private JButton btnGenerar, btnExportar, btnRegresar;
    private JPanel cardPanel;

    private final SupabaseService service;
    private final Solicitante solicitante;

    private String numeroLicenciaGenerado;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;

    // PALETA DE COLORES
    private final Color COLOR_PRIMARY = new Color(30, 58, 138); // Azul Institucional
    private final Color COLOR_BORDER = new Color(200, 200, 200);

    public GenerarLicencia(String cedula) {
        this.service = new SupabaseService();
        this.solicitante = service.obtenerTodosLosTramites().stream()
                .filter(s -> s.getCedula().equals(cedula))
                .findFirst().orElse(null);

        setContentPane(panelPrincipal);
        setTitle("Emisión de Licencia Oficial");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        if(solicitante == null) {
            JOptionPane.showMessageDialog(this, "Error: Solicitante no encontrado.");
            dispose();
            return;
        }

        calcularDatosLicencia();
        personalizarUI();
        cargarDatosVisuales();
        iniciarLogica();
    }

    private void calcularDatosLicencia() {
        this.fechaEmision = LocalDate.now();
        this.fechaVencimiento = fechaEmision.plusYears(5);
        this.numeroLicenciaGenerado = solicitante.getCedula() + "-" + LocalDate.now().getYear();
    }

    private void personalizarUI() {
        // Estilo de inputs readonly
        estilizarInputReadonly(txtEmision);
        estilizarInputReadonly(txtVencimiento);

        // Estilo de Foto
        lblFoto.setOpaque(true);
        lblFoto.setBorder(new LineBorder(Color.BLACK, 1));

        // Estilo Header Tarjeta
        if(cardPanel != null) {
            cardPanel.setBorder(new CompoundBorder(
                    new LineBorder(new Color(200,200,200), 1),
                    new MatteBorder(25, 0, 0, 0, COLOR_PRIMARY)
            ));
        }

        // === BOTONES ===
        estilizarBoton(btnGenerar, COLOR_PRIMARY, Color.WHITE); // Principal

        estilizarBoton(btnExportar, Color.WHITE, COLOR_PRIMARY); // Secundario
        btnExportar.setBorder(new LineBorder(COLOR_PRIMARY, 2));

        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY); // Regresar
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER, 1));
        btnRegresar.setPreferredSize(new Dimension(150, 45));

        btnExportar.setEnabled(false);
    }

    private void estilizarInputReadonly(JTextField campo) {
        campo.setOpaque(true);
        campo.setBackground(Color.WHITE);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(5, 10, 5, 10)));
        campo.setEditable(false);
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                g2.dispose();
                super.paint(g, c);
            }
            @Override
            protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(b.getForeground());
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(b.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = textRect.x + (textRect.width - fm.stringWidth(text)) / 2;
                int y = textRect.y + fm.getAscent();
                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { if(btn.isEnabled()) { btn.setBackground(bg.darker()); btn.setForeground(fg); } }
            @Override
            public void mouseExited(MouseEvent e) { if(btn.isEnabled()) { btn.setBackground(bg); btn.setForeground(fg); } }
        });
    }

    private void cargarDatosVisuales() {
        String htmlDatos = "<html>" +
                "<div style='font-family: Segoe UI;'>" +
                "<h3 style='margin: 0; color: #000;'>REPÚBLICA DEL ECUADOR</h3>" +
                "<br>" +
                "<b>APELLIDOS Y NOMBRES:</b><br>" +
                "<span style='font-size: 14px;'>" + solicitante.getNombres().toUpperCase() + " " + solicitante.getApellidos().toUpperCase() + "</span><br><br>" +
                "<b>CÉDULA DE IDENTIDAD:</b><br>" +
                "<span style='font-size: 14px;'>" + solicitante.getCedula() + "</span><br><br>" +
                "<b>TIPO DE LICENCIA:</b>   <span style='font-size: 18px; color: red; font-weight: bold;'> TIPO " + solicitante.getTipoLicencia() + "</span><br><br>" +
                "<b>DONANTE:</b> SI<br>" +
                "</div>" +
                "</html>";

        lblDatosLicencia.setText(htmlDatos);
        lblBarcode.setText("*" + numeroLicenciaGenerado + "*");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtEmision.setText(fechaEmision.format(fmt));
        txtVencimiento.setText(fechaVencimiento.format(fmt));

        cargarImagen(solicitante.getFotoUrl());
    }

    private void cargarImagen(String url) {
        if(url == null || url.isEmpty()) return;
        new Thread(() -> {
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                Image dimg = img.getScaledInstance(130, 160, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> lblFoto.setIcon(new ImageIcon(dimg)));
            } catch (Exception ignored) {}
        }).start();
    }

    private void iniciarLogica() {
        btnRegresar.addActionListener(e -> dispose());

        btnGenerar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Confirmar emisión de licencia?\nEsta acción es irreversible.",
                    "Emisión Oficial", JOptionPane.YES_NO_OPTION);

            if(confirm == JOptionPane.YES_OPTION) {
                boolean exito = service.actualizarEstadoSolicitante(solicitante.getCedula(), "LICENCIA_EMITIDA", "Licencia No: " + numeroLicenciaGenerado);
                if(exito) {
                    JOptionPane.showMessageDialog(this, "Licencia emitida y registrada exitosamente.");
                    btnGenerar.setEnabled(false);
                    btnGenerar.setText("Licencia Emitida");
                    btnExportar.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Error de comunicación con la base de datos.");
                }
            }
        });

        // --- LÓGICA DE EXPORTACIÓN A DESCARGAS ---
        btnExportar.addActionListener(e -> {
            try {
                // 1. Obtener ruta de descargas del usuario
                String userHome = System.getProperty("user.home");
                File downloadsFolder = new File(userHome, "Downloads");

                // Fallback si no encuentra Downloads (ej: sistema en otro idioma que no sea inglés estándar, aunque Java suele mapearlo)
                if (!downloadsFolder.exists()) {
                    downloadsFolder = new File(userHome, "Descargas");
                }
                if (!downloadsFolder.exists()) {
                    downloadsFolder = new File(userHome); // Si falla todo, usa el home
                }

                // 2. Definir nombre del archivo
                String fileName = "Licencia_" + solicitante.getCedula() + ".pdf";
                File targetFile = new File(downloadsFolder, fileName);

                // 3. Generar PDF
                boolean exito = PDFGenerator.generarLicencia(solicitante, targetFile.getAbsolutePath());

                if (exito) {
                    int open = JOptionPane.showConfirmDialog(this,
                            "PDF Guardado en: " + targetFile.getAbsolutePath() + "\n¿Desea abrirlo ahora?",
                            "Exportación Exitosa", JOptionPane.YES_NO_OPTION);

                    if (open == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(targetFile);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo automáticamente.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error al generar el archivo PDF.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage());
            }
        });
    }
}
