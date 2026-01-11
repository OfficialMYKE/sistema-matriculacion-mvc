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

    // COMPONENTES UI
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

    // BANDERA DE CONTROL (Para evitar la ventana fantasma)
    private boolean inicializacionExitosa = false;

    // PALETA DE COLORES
    private final Color COLOR_PRIMARY = new Color(30, 58, 138);
    private final Color COLOR_BORDER = new Color(200, 200, 200);

    public GenerarLicencia(String cedula) {
        this.service = new SupabaseService();

        // BUSCAR USUARIO
        this.solicitante = service.obtenerTodosLosTramites().stream()
                .filter(s -> s.getCedula().equals(cedula))
                .findFirst().orElse(null);

        // VALIDAR EXISTENCIA
        if (solicitante == null) {
            // Mostramos el mensaje
            JOptionPane.showMessageDialog(null,
                    "Error: No se encontró un solicitante con la cédula " + cedula + "\nVerifique que el trámite exista.",
                    "Usuario No Encontrado",
                    JOptionPane.ERROR_MESSAGE);

            // Marcamos como fallido para que setVisible no funcione
            this.inicializacionExitosa = false;

            // Destruimos la instancia actual
            this.dispose();
            return;
        }

        this.inicializacionExitosa = true;

        // CONFIGURACIÓN DE LA VENTANA
        setTitle("Emisión de Licencia Oficial");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        calcularDatosLicencia();
        construirInterfaz();
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        cargarDatosVisuales();
        iniciarLogica();
    }


    @Override
    public void setVisible(boolean b) {
        // Si la inicialización falló (usuario no existe), bloqueamos que la ventana se muestre
        if (b && !inicializacionExitosa) {
            super.dispose(); // Aseguramos que muera
            return; // No hacemos nada más (no llamamos a super.setVisible)
        }
        super.setVisible(b);
    }


    private void calcularDatosLicencia() {
        this.fechaEmision = LocalDate.now();
        this.fechaVencimiento = fechaEmision.plusYears(5);
        this.numeroLicenciaGenerado = solicitante.getCedula() + "-" + LocalDate.now().getYear();
    }

    private void construirInterfaz() {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(245, 247, 250));
        setContentPane(panelPrincipal);

        // A. HEADER AZUL
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PRIMARY);
        pnlHeader.setPreferredSize(new Dimension(getWidth(), 80));
        pnlHeader.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("Generar Licencia - " + solicitante.getCedula());
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        panelPrincipal.add(pnlHeader, BorderLayout.NORTH);

        // B. CONTENIDO CENTRAL
        JPanel pnlCentro = new JPanel(new GridBagLayout());
        pnlCentro.setOpaque(false);

        cardPanel = new JPanel(new BorderLayout(20, 0));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(600, 350));
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200,200,200), 1),
                new MatteBorder(30, 0, 0, 0, COLOR_PRIMARY)
        ));

        JPanel pnlCardContent = new JPanel(new GridBagLayout());
        pnlCardContent.setBackground(Color.WHITE);
        pnlCardContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 4;
        lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(130, 160));
        lblFoto.setOpaque(true);
        lblFoto.setBackground(new Color(230, 230, 230));
        lblFoto.setBorder(new LineBorder(Color.BLACK, 1));
        pnlCardContent.add(lblFoto, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        lblDatosLicencia = new JLabel("Cargando datos...");
        pnlCardContent.add(lblDatosLicencia, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        JPanel pnlFechas = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlFechas.setBackground(Color.WHITE);

        txtEmision = new JTextField(); estilizarInputReadonly(txtEmision);
        txtVencimiento = new JTextField(); estilizarInputReadonly(txtVencimiento);

        pnlFechas.add(crearGrupoFecha("Emisión:", txtEmision));
        pnlFechas.add(crearGrupoFecha("Vence:", txtVencimiento));
        pnlCardContent.add(pnlFechas, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        lblBarcode = new JLabel("*CODIGO*");
        lblBarcode.setFont(new Font("Consolas", Font.PLAIN, 28));
        lblBarcode.setForeground(Color.DARK_GRAY);
        pnlCardContent.add(lblBarcode, gbc);

        cardPanel.add(pnlCardContent, BorderLayout.CENTER);
        pnlCentro.add(cardPanel);

        panelPrincipal.add(pnlCentro, BorderLayout.CENTER);

        // C. PANEL INFERIOR
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        pnlBotones.setBackground(Color.WHITE);
        pnlBotones.setBorder(new MatteBorder(1, 0, 0, 0, COLOR_BORDER));

        btnRegresar = new JButton("Regresar al Menú");
        btnGenerar = new JButton("Generar Licencia");
        btnExportar = new JButton("Exportar PDF");

        // Estilos
        btnRegresar.setPreferredSize(new Dimension(160, 45));
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER, 1));

        btnGenerar.setPreferredSize(new Dimension(180, 45));
        estilizarBoton(btnGenerar, COLOR_PRIMARY, Color.WHITE);

        btnExportar.setPreferredSize(new Dimension(160, 45));
        estilizarBoton(btnExportar, Color.WHITE, COLOR_PRIMARY);
        btnExportar.setBorder(new LineBorder(COLOR_PRIMARY, 2));

        pnlBotones.add(btnRegresar);
        pnlBotones.add(btnGenerar);
        pnlBotones.add(btnExportar);

        panelPrincipal.add(pnlBotones, BorderLayout.SOUTH);
    }

    private JPanel crearGrupoFecha(String label, JTextField campo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(Color.GRAY);
        p.add(l, BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private void estilizarInputReadonly(JTextField campo) {
        campo.setOpaque(false);
        campo.setBackground(Color.WHITE);
        campo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        campo.setBorder(null);
        campo.setEditable(false);
        campo.setForeground(Color.BLACK);
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        if(btn.getBorder() == null) btn.setBorderPainted(false);
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
                    g2.setColor(new Color(220, 220, 220));
                }
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                g2.dispose();

                paintTextManual(g, c, ((AbstractButton)c).getText());
            }

            private void paintTextManual(Graphics g, JComponent c, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (c.isEnabled()) {
                    g2.setColor(c.getForeground());
                } else {
                    g2.setColor(Color.GRAY);
                }

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
                if(btn.isEnabled()) {
                    if (bg.equals(Color.WHITE)) btn.putClientProperty("bgColor", new Color(240, 240, 240));
                    else btn.putClientProperty("bgColor", bg.darker());
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

    private void cargarDatosVisuales() {
        if (solicitante == null) return; // Protección extra

        String htmlDatos = "<html>" +
                "<div style='font-family: Segoe UI; padding: 5px;'>" +
                "<h3 style='margin: 0; color: #1E3A8A;'>REPÚBLICA DEL ECUADOR</h3>" +
                "<span style='font-size: 10px; color: gray;'>AGENCIA NACIONAL DE TRÁNSITO</span><br><br>" +
                "<b>APELLIDOS Y NOMBRES:</b><br>" +
                "<span style='font-size: 14px;'>" + solicitante.getNombres().toUpperCase() + " " + solicitante.getApellidos().toUpperCase() + "</span><br><br>" +
                "<b>CÉDULA DE IDENTIDAD:</b> &nbsp;&nbsp; <b>DONANTE:</b><br>" +
                "<span style='font-size: 14px;'>" + solicitante.getCedula() + "</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; " +
                "<span style='font-size: 14px;'>" + (solicitante.isEsDonante() ? "SI" : "NO") + "</span><br><br>" +
                "<b>TIPO LICENCIA:</b>   <span style='font-size: 18px; color: #D32F2F; font-weight: bold;'> " + solicitante.getTipoLicencia() + "</span>" +
                "</div>" +
                "</html>";

        lblDatosLicencia.setText(htmlDatos);
        lblBarcode.setText("|| ||| || ||| " + numeroLicenciaGenerado.replace("-", "") + " || ||");

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

        if (solicitante != null) {
            boolean yaEmitida = "LICENCIA_EMITIDA".equals(solicitante.getEstado());
            btnGenerar.setEnabled(!yaEmitida);
            btnExportar.setEnabled(yaEmitida);
            if(yaEmitida) btnGenerar.setText("Licencia Emitida");
        }

        btnGenerar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Confirmar emisión de licencia?\nEsta acción quedará registrada.",
                    "Emisión Oficial", JOptionPane.YES_NO_OPTION);

            if(confirm == JOptionPane.YES_OPTION) {
                boolean exito = service.actualizarEstadoSolicitante(solicitante.getCedula(), "LICENCIA_EMITIDA", "Licencia No: " + numeroLicenciaGenerado);
                if(exito) {
                    JOptionPane.showMessageDialog(this, "¡Licencia Generada Exitosamente!");
                    btnGenerar.setEnabled(false);
                    btnGenerar.setText("Licencia Emitida");
                    btnExportar.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar en base de datos.");
                }
            }
        });

        btnExportar.addActionListener(e -> {
            try {
                String userHome = System.getProperty("user.home");
                File downloadsFolder = new File(userHome, "Downloads");
                if (!downloadsFolder.exists()) downloadsFolder = new File(userHome, "Descargas");
                if (!downloadsFolder.exists()) downloadsFolder = new File(userHome);

                String fileName = "Licencia_" + solicitante.getCedula() + ".pdf";
                File targetFile = new File(downloadsFolder, fileName);

                boolean exito = PDFGenerator.generarLicencia(solicitante, targetFile.getAbsolutePath());

                if (exito) {
                    int op = JOptionPane.showConfirmDialog(this, "PDF Guardado en Descargas.\n¿Abrir ahora?", "Exportado", JOptionPane.YES_NO_OPTION);
                    if (op == JOptionPane.YES_OPTION) Desktop.getDesktop().open(targetFile);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al generar PDF.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}