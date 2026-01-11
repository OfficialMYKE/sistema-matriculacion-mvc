package com.licencias.sistemalicenciasfx.view;

import com.licencias.sistemalicenciasfx.model.entities.Solicitante;
import com.licencias.sistemalicenciasfx.service.SupabaseService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GestionTramites extends JFrame {

    private JPanel panelPrincipal;

    // TABLA Y HERRAMIENTAS DE FILTRADO
    private JTable tblTramites;
    private DefaultTableModel tableModel; // Los datos
    private TableRowSorter<DefaultTableModel> sorter; // El filtro (importante)

    // CONTROLES DE FILTRO
    private JComboBox<String> cmbFiltroEstado;
    private JTextField txtBusqueda;

    // BOTONES
    private JButton btnActualizar;
    private JButton btnVerDetalle;
    private JButton btnRegresar;

    // SERVICIO DE DATOS
    private final SupabaseService supabaseService;

    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138);

    public GestionTramites() {
        this.supabaseService = new SupabaseService();

        setContentPane(panelPrincipal);
        setTitle("Centro de Gestión de Trámites");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa

        // 1. Preparamos el diseño visual
        personalizarUI();

        // 2. Preparamos la tabla y el filtro
        inicializarTabla();
        configurarFiltros();

        // 3. Activamos los botones
        iniciarLogica();

        // 4. Traemos los datos de la nube
        cargarDatos();
    }

    // PARTE VISUAL
    private void personalizarUI() {
        estilizarInput(txtBusqueda);

        cmbFiltroEstado.setBackground(COLOR_BG_INPUT);
        cmbFiltroEstado.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        cmbFiltroEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Botones Azules
        estilizarBoton(btnVerDetalle, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnActualizar, COLOR_ACCENT, Color.WHITE);

        // Botón Regresar (Blanco con borde gris)
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
    }

    private void estilizarInput(JTextField campo) {
        campo.setOpaque(true);
        campo.setBackground(COLOR_BG_INPUT);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(8, 12, 8, 12)));

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBackground(Color.WHITE);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_ACCENT, 2), new EmptyBorder(7, 11, 7, 11)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                campo.setBackground(COLOR_BG_INPUT);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    // Método para botones redondos
    private void estilizarBoton(JButton btn, Color bg, Color fg) {
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
                if(btn.isEnabled()){
                    if (!bg.equals(Color.WHITE)) btn.putClientProperty("bgColor", bg.darker());
                    else btn.putClientProperty("bgColor", new Color(240, 240, 240));
                    btn.repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                if(btn.isEnabled()){
                    btn.putClientProperty("bgColor", bg);
                    btn.repaint();
                }
            }
        });
    }

    // TABLA Y FILTROS
    private void inicializarTabla() {
        String[] columnas = {"#", "CÉDULA", "NOMBRE COMPLETO", "TIPO", "ESTADO ACTUAL"};

        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTramites.setModel(tableModel);

        // Ajustes visuales
        tblTramites.setRowHeight(35);
        tblTramites.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblTramites.setSelectionBackground(new Color(232, 240, 254));
        tblTramites.setSelectionForeground(Color.BLACK);
        tblTramites.setShowVerticalLines(false);
        tblTramites.setIntercellSpacing(new Dimension(0, 0));

        // Cabecera
        tblTramites.getTableHeader().setBackground(COLOR_ACCENT);
        tblTramites.getTableHeader().setForeground(Color.WHITE);
        tblTramites.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblTramites.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Centrado de columnas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblTramites.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblTramites.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblTramites.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblTramites.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Conectamos el filtro a la tabla
        sorter = new TableRowSorter<>(tableModel);
        tblTramites.setRowSorter(sorter);
    }

    private void configurarFiltros() {
        // Estas son las opciones del combobox.
        String[] estados = {"TODOS", "PENDIENTE", "EN_EXAMENES", "APROBADO", "REPROBADO", "LICENCIA_EMITIDA"};
        cmbFiltroEstado.removeAllItems();
        for(String est : estados) cmbFiltroEstado.addItem(est);
    }

    private void cargarDatos() {
        btnActualizar.setEnabled(false);
        new Thread(() -> {
            try {
                List<Solicitante> lista = supabaseService.obtenerTodosLosTramites();

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    int contador = 1;
                    for (Solicitante s : lista) {
                        tableModel.addRow(new Object[]{
                                contador++,
                                s.getCedula(),
                                s.getNombreCompleto(),
                                s.getTipoLicencia(),
                                s.getEstado().toUpperCase() // Guardamos en mayúsculas
                        });
                    }
                    filtrar();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> btnActualizar.setEnabled(true));
            }
        }).start();
    }

    private void iniciarLogica() {
        btnRegresar.addActionListener(e -> this.dispose());
        btnActualizar.addActionListener(e -> cargarDatos());

        // Eventos de filtrado
        txtBusqueda.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { filtrar(); }
        });
        cmbFiltroEstado.addActionListener(e -> filtrar());

        // Navegación Inteligente (Switch por estado)
        btnVerDetalle.addActionListener(e -> {
            int row = tblTramites.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, " Seleccione un trámite de la lista para ver detalles.");
                return;
            }

            int modelRow = tblTramites.convertRowIndexToModel(row);
            String cedula = (String) tableModel.getValueAt(modelRow, 1);
            String estado = (String) tableModel.getValueAt(modelRow, 4);

            switch(estado) {
                case "PENDIENTE":
                    JOptionPane.showMessageDialog(this, "Trámite Pendiente: Abriendo Verificación de Requisitos...");
                    new VerificacionRequisitos().setVisible(true);
                    break;
                case "EN_EXAMENES":
                    JOptionPane.showMessageDialog(this, "Trámite en Exámenes: Abriendo Registro de Calificaciones...");
                    new RegistroExamenes().setVisible(true);
                    break;
                case "APROBADO":
                case "LICENCIA_EMITIDA":
                    new GenerarLicencia(cedula).setVisible(true);
                    break;
                default:
                    new DetalleTramite(cedula).setVisible(true);
            }
        });
    }

    // FILTRO
    private void filtrar() {
        // Texto del buscador
        String texto = txtBusqueda.getText().trim().toLowerCase();

        // Selección del combo
        String estadoSeleccionado = (String) cmbFiltroEstado.getSelectedItem();
        if (estadoSeleccionado == null) return;

        // Crear el Filtro
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Obtenemos los valores de la fila
                String cedula = entry.getStringValue(1).toLowerCase();
                String nombre = entry.getStringValue(2).toLowerCase();

                // Obtenemos el Estado de la fila y lo "Limpiamos"
                // Columna 4 es el Estado
                String estadoFila = entry.getStringValue(4);
                if (estadoFila == null) estadoFila = "";

                // Normalizamos: Mayúsculas y quitamos espacios
                String estadoFilaNormal = estadoFila.toUpperCase().trim();
                String estadoComboNormal = estadoSeleccionado.toUpperCase().trim();

                // Lógica de coincidencia de TEXTO
                boolean coincideTexto = cedula.contains(texto) || nombre.contains(texto);

                // Lógica de coincidencia de ESTADO
                boolean coincideEstado = false;

                if (estadoComboNormal.equals("TODOS")) {
                    coincideEstado = true;
                } else {
                    // Reemplazamos guiones bajos por espacios para que coincidan
                    // Ejemplo: "EN_EXAMENES" será igual a "EN EXAMENES"
                    String f1 = estadoFilaNormal.replace("_", " ");
                    String f2 = estadoComboNormal.replace("_", " ");

                    coincideEstado = f1.equals(f2);
                }

                return coincideTexto && coincideEstado;
            }
        };

        sorter.setRowFilter(rf);
    }
}