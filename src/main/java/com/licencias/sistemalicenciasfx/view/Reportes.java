package com.licencias.sistemalicenciasfx.view;

import com.licencias.sistemalicenciasfx.model.entities.Solicitante;
import com.licencias.sistemalicenciasfx.service.SupabaseService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

public class Reportes extends JFrame {

    // COLORES CORPORATIVOS
    private final Color COLOR_HEADER = new Color(30, 58, 138); // Azul EPN
    private final Color COLOR_BG_PANEL = Color.WHITE;
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER = new Color(200, 200, 200);

    // Colores de Botones
    private final Color COLOR_BTN_PRIMARY = new Color(30, 58, 138); // Azul
    private final Color COLOR_BTN_SUCCESS = new Color(40, 167, 69); // Verde
    private final Color COLOR_BTN_WARNING = new Color(255, 193, 7); // Amarillo
    private final Color COLOR_BTN_DANGER = new Color(220, 53, 69);  // Rojo
    private final Color COLOR_BTN_GRAY = new Color(108, 117, 125);  // Gris

    // COMPONENTES UI
    private JPanel panelPrincipal;

    // Filtros
    private JTextField txtFechaDesde, txtFechaHasta, txtCedula;
    private JComboBox<String> cmbEstado, cmbTipo;
    private JButton btnBuscar, btnLimpiar;

    // KPIs (Tarjetas)
    private JLabel lblTotal, lblAprobados, lblRechazados, lblPendientes;

    // Tabla
    private JTable tablaReportes;
    private DefaultTableModel modeloTabla;

    // Botones Pie
    private JButton btnExportar, btnVerDetalle, btnRegresar;

    // Datos
    private final SupabaseService service;
    private List<Solicitante> listaCompleta;

    public Reportes() {
        this.service = new SupabaseService();

        setTitle("Reportes y Estadísticas - EPN");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        construirInterfaz();
        cargarDatos();
        iniciarLogica();
    }

    private void construirInterfaz() {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(COLOR_BG_PANEL);
        setContentPane(panelPrincipal);

        // 1. HEADER
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_HEADER);
        pnlHeader.setPreferredSize(new Dimension(getWidth(), 80));
        pnlHeader.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("Reportes y Estadísticas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);
        panelPrincipal.add(pnlHeader, BorderLayout.NORTH);

        // 2. CONTENIDO CENTRAL
        JPanel pnlContenido = new JPanel(new BorderLayout(0, 20));
        pnlContenido.setBackground(new Color(245, 247, 250));
        pnlContenido.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel Superior (Filtros + KPIs)
        JPanel pnlTop = new JPanel(new BorderLayout(0, 15));
        pnlTop.setOpaque(false);
        pnlTop.add(construirPanelFiltros(), BorderLayout.NORTH);
        pnlTop.add(construirPanelKPIs(), BorderLayout.CENTER);

        pnlContenido.add(pnlTop, BorderLayout.NORTH);

        // Tabla
        pnlContenido.add(construirPanelTabla(), BorderLayout.CENTER);

        panelPrincipal.add(pnlContenido, BorderLayout.CENTER);

        // FOOTER
        panelPrincipal.add(construirPanelFooter(), BorderLayout.SOUTH);
    }

    private JPanel construirPanelFiltros() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new CompoundBorder(
                new LineBorder(new Color(230,230,230), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Fila 1: Etiquetas
        gbc.gridy = 0;
        gbc.gridx = 0; pnl.add(crearLabel("Fecha Desde"), gbc);
        gbc.gridx = 1; pnl.add(crearLabel("Fecha Hasta"), gbc);
        gbc.gridx = 2; pnl.add(crearLabel("Estado"), gbc);
        gbc.gridx = 3; pnl.add(crearLabel("Tipo Licencia"), gbc);
        gbc.gridx = 4; pnl.add(crearLabel("Cédula"), gbc);

        // Fila 2: Inputs
        gbc.gridy = 1;

        gbc.gridx = 0; txtFechaDesde = crearInput(); pnl.add(txtFechaDesde, gbc);
        gbc.gridx = 1; txtFechaHasta = crearInput(); pnl.add(txtFechaHasta, gbc);

        gbc.gridx = 2;
        cmbEstado = new JComboBox<>(new String[]{"TODOS", "PENDIENTE", "EN_EXAMENES", "APROBADO", "REPROBADO", "LICENCIA_EMITIDA"});
        estilizarCombo(cmbEstado);
        pnl.add(cmbEstado, gbc);

        gbc.gridx = 3;
        cmbTipo = new JComboBox<>(new String[]{"TODOS", "A", "B", "C", "D", "E", "F", "G"});
        estilizarCombo(cmbTipo);
        pnl.add(cmbTipo, gbc);

        gbc.gridx = 4; txtCedula = crearInput(); pnl.add(txtCedula, gbc);

        // Botones Filtro
        gbc.gridx = 5;
        gbc.weightx = 0;
        JPanel pnlBtns = new JPanel(new GridLayout(1, 2, 5, 0));
        pnlBtns.setOpaque(false);

        btnLimpiar = new JButton("Limpiar");
        estilizarBoton(btnLimpiar, COLOR_BTN_GRAY, Color.WHITE);
        btnLimpiar.setPreferredSize(new Dimension(90, 35));

        btnBuscar = new JButton("Buscar");
        estilizarBoton(btnBuscar, COLOR_BTN_PRIMARY, Color.WHITE);
        btnBuscar.setPreferredSize(new Dimension(90, 35));

        pnlBtns.add(btnLimpiar);
        pnlBtns.add(btnBuscar);
        pnl.add(pnlBtns, gbc);

        return pnl;
    }

    private JPanel construirPanelKPIs() {
        JPanel pnl = new JPanel(new GridLayout(1, 4, 20, 0));
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(0, 90));

        lblTotal = crearTarjeta("TOTAL TRÁMITES", COLOR_BTN_PRIMARY);
        lblAprobados = crearTarjeta("EMITIDAS", COLOR_BTN_SUCCESS);
        lblRechazados = crearTarjeta("REPROBADOS", COLOR_BTN_DANGER);
        lblPendientes = crearTarjeta("EN PROCESO", COLOR_BTN_WARNING);

        pnl.add(crearContainerTarjeta(lblTotal));
        pnl.add(crearContainerTarjeta(lblAprobados));
        pnl.add(crearContainerTarjeta(lblRechazados));
        pnl.add(crearContainerTarjeta(lblPendientes));

        return pnl;
    }

    private JPanel crearContainerTarjeta(JLabel lbl) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JLabel crearTarjeta(String titulo, Color colorBorde) {
        JLabel lbl = new JLabel("0", SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(Color.WHITE);
        lbl.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new CompoundBorder(
                        new MatteBorder(5, 0, 0, 0, colorBorde),
                        new EmptyBorder(5, 0, 10, 0)
                )
        ));
        lbl.setText("<html><center><span style='font-size:11px; color:gray; font-family:Segoe UI;'>" + titulo + "</span><br>" +
                "<span style='font-size:22px; color:black; font-family:Segoe UI;'>0</span></center></html>");
        return lbl;
    }

    private JPanel construirPanelTabla() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        modeloTabla = new DefaultTableModel(new Object[]{"Cédula", "Nombre Completo", "Tipo", "Estado", "Email"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tablaReportes = new JTable(modeloTabla);
        estilizarTabla(tablaReportes);

        JScrollPane scroll = new JScrollPane(tablaReportes);
        scroll.setBorder(new LineBorder(COLOR_BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel construirPanelFooter() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new MatteBorder(1, 0, 0, 0, COLOR_BORDER));

        // BOTÓN REGRESAR (ESTILO IDÉNTICO A GESTIONUSUARIOS)
        btnRegresar = new JButton("Regresar al Menú");

        // Dimensión: Altura 45px (Igual que en GestionUsuarios)
        // Ancho 180px para que se vea bien en el footer
        btnRegresar.setPreferredSize(new Dimension(180, 45));

        // Colores y Borde: Fondo Blanco, Texto DarkGray, Borde Gris
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER, 1));

        // Botones de acción normales
        btnVerDetalle = new JButton("Ver Detalle");
        estilizarBoton(btnVerDetalle, COLOR_BTN_PRIMARY, Color.WHITE);
        btnVerDetalle.setPreferredSize(new Dimension(140, 45)); // Ajusté altura a 45 para uniformidad

        btnExportar = new JButton("Exportar CSV");
        estilizarBoton(btnExportar, COLOR_BTN_SUCCESS, Color.WHITE);
        btnExportar.setPreferredSize(new Dimension(140, 45));

        pnl.add(btnRegresar);
        pnl.add(btnVerDetalle);
        pnl.add(btnExportar);

        return pnl;
    }

    // LÓGICA
    private void cargarDatos() {
        new Thread(() -> {
            try {
                listaCompleta = service.obtenerTodosLosTramites();
                SwingUtilities.invokeLater(this::aplicarFiltros);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void iniciarLogica() {
        btnBuscar.addActionListener(e -> aplicarFiltros());

        btnLimpiar.addActionListener(e -> {
            txtFechaDesde.setText(""); txtFechaHasta.setText(""); txtCedula.setText("");
            cmbEstado.setSelectedIndex(0); cmbTipo.setSelectedIndex(0);
            aplicarFiltros();
        });

        btnRegresar.addActionListener(e -> dispose());

        btnVerDetalle.addActionListener(e -> {
            int row = tablaReportes.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione una fila.");
                return;
            }

            String cedula = (String) modeloTabla.getValueAt(row, 0);

            // Buscar solicitante en la lista completa
            Solicitante s = listaCompleta.stream()
                    .filter(sol -> sol.getCedula().equals(cedula))
                    .findFirst().orElse(null);

            if (s == null) {
                JOptionPane.showMessageDialog(this, "No se encontró el solicitante.");
                return;
            }

            // Si tiene licencia emitida, abrir GenerarLicencia
            if ("LICENCIA_EMITIDA".equalsIgnoreCase(s.getEstado())) {
                GenerarLicencia ventana = new GenerarLicencia(s.getCedula());

                if (!ventana.isInicializacionExitosa()) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede abrir la licencia.\nVerifique los datos del solicitante.",
                            "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ventana.setVisible(true);
            } else {
                // Construir mensaje con datos del solicitante
                StringBuilder mensaje = new StringBuilder("<html>");
                mensaje.append("<b>Nombre Completo:</b> ").append(s.getNombreCompleto()).append("<br>");
                mensaje.append("<b>Cédula:</b> ").append(s.getCedula()).append("<br>");
                mensaje.append("<b>Tipo de Licencia:</b> ").append(s.getTipoLicencia()).append("<br>");
                mensaje.append("<b>Estado:</b> ").append(s.getEstado()).append("<br>");
                mensaje.append("<b>Email:</b> ").append(s.getEmail()).append("<br>");
                mensaje.append("<b>Licencia:</b> No emitida<br>");
                mensaje.append("</html>");

                JOptionPane.showMessageDialog(this, mensaje.toString(), "Detalle del Trámite", JOptionPane.INFORMATION_MESSAGE);
            }
        });



        btnExportar.addActionListener(e -> exportarCSV());
    }

    private void aplicarFiltros() {
        if(listaCompleta == null) return;

        String fDesde = txtFechaDesde.getText().trim();
        String fHasta = txtFechaHasta.getText().trim();
        String est = (String) cmbEstado.getSelectedItem();
        String tip = (String) cmbTipo.getSelectedItem();
        String ced = txtCedula.getText().trim();

        List<Solicitante> filtrados = listaCompleta.stream()
                .filter(s -> est.equals("TODOS") || s.getEstado().equalsIgnoreCase(est))
                .filter(s -> tip.equals("TODOS") || s.getTipoLicencia().equalsIgnoreCase(tip))
                .filter(s -> ced.isEmpty() || s.getCedula().startsWith(ced))
                .collect(Collectors.toList());

        modeloTabla.setRowCount(0);
        for(Solicitante s : filtrados) {
            modeloTabla.addRow(new Object[]{s.getCedula(), s.getNombreCompleto(), s.getTipoLicencia(), s.getEstado(), s.getEmail()});
        }
        actualizarKPIs(filtrados);
    }

    private void actualizarKPIs(List<Solicitante> datos) {
        long total = datos.size();
        long emitidas = datos.stream().filter(s -> s.getEstado().equalsIgnoreCase("LICENCIA_EMITIDA")).count();
        long rechazados = datos.stream().filter(s -> s.getEstado().equalsIgnoreCase("REPROBADO")).count();
        long pendientes = total - emitidas - rechazados;

        actualizarTextoKPI(lblTotal, "TOTAL TRÁMITES", total);
        actualizarTextoKPI(lblAprobados, "EMITIDAS", emitidas);
        actualizarTextoKPI(lblRechazados, "REPROBADOS", rechazados);
        actualizarTextoKPI(lblPendientes, "EN PROCESO", pendientes);
    }

    private void actualizarTextoKPI(JLabel lbl, String tit, long val) {
        lbl.setText("<html><center><span style='font-size:11px; color:gray; font-family:Segoe UI;'>" + tit + "</span><br>" +
                "<span style='font-size:22px; color:black; font-family:Segoe UI;'>" + val + "</span></center></html>");
    }

    private void exportarCSV() {
        if (tablaReportes.getRowCount() == 0) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("reporte.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()))) {
                for (int i = 0; i < modeloTabla.getColumnCount(); i++) bw.write(modeloTabla.getColumnName(i) + ",");
                bw.newLine();
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    for (int j = 0; j < modeloTabla.getColumnCount(); j++) bw.write(modeloTabla.getValueAt(i, j) + ",");
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "Exportado correctamente.");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // --- MÉTODOS DE ESTILO (COPIADOS EXACTAMENTE DE GESTION USUARIOS) ---

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100));
        return lbl;
    }

    private JTextField crearInput() {
        JTextField txt = new JTextField();
        estilizarInput(txt);
        return txt;
    }

    private void estilizarInput(JTextField cmp) {
        cmp.setPreferredSize(new Dimension(100, 35));
        cmp.setBackground(COLOR_BG_INPUT);
        cmp.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER), new EmptyBorder(5, 10, 5, 10)));
        cmp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmp.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                cmp.setBackground(Color.WHITE);
                cmp.setBorder(new CompoundBorder(new LineBorder(COLOR_HEADER, 2), new EmptyBorder(4, 9, 4, 9)));
            }
            public void focusLost(FocusEvent e) {
                cmp.setBackground(COLOR_BG_INPUT);
                cmp.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER), new EmptyBorder(5, 10, 5, 10)));
            }
        });
    }

    private void estilizarCombo(JComboBox cmp) {
        cmp.setPreferredSize(new Dimension(100, 35));
        cmp.setBackground(COLOR_BG_INPUT);
        cmp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    // ESTILO DE BOTÓN EXACTO DE GESTION USUARIOS
    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        // Si no tiene borde manual, quitamos el pintado por defecto
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
                    g2.setColor(new Color(200, 200, 200));
                }

                // AQUÍ ESTÁ LA CLAVE: Radio de 20px
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                g2.dispose();

                paintTextManual(g, c, ((AbstractButton)c).getText());
            }

            private void paintTextManual(Graphics g, JComponent c, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Usar c.getForeground() permite que funcione para BLANCO y DARK_GRAY
                g2.setColor(c.getForeground());

                g2.setFont(c.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();

                int x = (c.getWidth() - textWidth) / 2;
                int y = (c.getHeight() + textHeight) / 2 - 2;

                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if(btn.isEnabled()) {
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

    private void estilizarTabla(JTable tabla) {
        tabla.setBackground(Color.WHITE);
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(230, 230, 230));
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setSelectionBackground(new Color(232, 240, 254));
        tabla.setSelectionForeground(Color.BLACK);

        JTableHeader header = tabla.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(COLOR_HEADER);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(new EmptyBorder(5, 5, 5, 5));
                return this;
            }
        });
    }
}