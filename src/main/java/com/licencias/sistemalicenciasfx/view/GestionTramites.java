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
    private JTable tblTramites;
    private JComboBox<String> cmbFiltroEstado;
    private JTextField txtBusqueda;
    private JButton btnActualizar;
    private JButton btnVerDetalle;
    private JButton btnRegresar;

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter; // Para filtrar
    private final SupabaseService supabaseService;

    // COLORES INSTITUCIONALES
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138);

    public GestionTramites() {
        this.supabaseService = new SupabaseService();

        setContentPane(panelPrincipal);
        setTitle("Centro de Gestión de Trámites");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        personalizarUI();
        inicializarTabla();
        configurarFiltros();
        iniciarLogica();

        // Cargar datos al iniciar
        cargarDatos();
    }

    private void personalizarUI() {
        estilizarInput(txtBusqueda);

        cmbFiltroEstado.setBackground(COLOR_BG_INPUT);
        cmbFiltroEstado.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        cmbFiltroEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        estilizarBoton(btnVerDetalle, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnActualizar, COLOR_ACCENT, Color.WHITE);

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

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
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
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
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
            public void mouseEntered(MouseEvent e) { if(btn.isEnabled()){ btn.setBackground(bg.darker()); btn.setForeground(fg); }}
            public void mouseExited(MouseEvent e) { if(btn.isEnabled()){ btn.setBackground(bg); btn.setForeground(fg); }}
        });
    }

    private void inicializarTabla() {
        String[] columnas = {"#", "CÉDULA", "NOMBRE COMPLETO", "TIPO", "ESTADO ACTUAL"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTramites.setModel(tableModel);

        // Configuración visual tabla
        tblTramites.setRowHeight(35);
        tblTramites.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblTramites.setSelectionBackground(new Color(232, 240, 254));
        tblTramites.setSelectionForeground(Color.BLACK);
        tblTramites.setShowVerticalLines(false);
        tblTramites.setIntercellSpacing(new Dimension(0, 0));

        // Header Azul
        tblTramites.getTableHeader().setBackground(COLOR_ACCENT);
        tblTramites.getTableHeader().setForeground(Color.WHITE);
        tblTramites.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblTramites.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Centrar columnas específicas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblTramites.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // #
        tblTramites.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Cédula
        tblTramites.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Tipo
        tblTramites.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Estado

        // Inicializar Sorter para el filtrado
        sorter = new TableRowSorter<>(tableModel);
        tblTramites.setRowSorter(sorter);
    }

    private void configurarFiltros() {
        String[] estados = {"TODOS", "PENDIENTE", "EN_EXAMENES", "APROBADO", "REPROBADO", "LICENCIA_EMITIDA"};
        cmbFiltroEstado.removeAllItems();
        for(String est : estados) cmbFiltroEstado.addItem(est);
    }

    private void cargarDatos() {
        btnActualizar.setEnabled(false); // Evitar doble click
        new Thread(() -> {
            try {
                // LLAMADA A BASE DE DATOS
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
                                s.getEstado().toUpperCase()
                        });
                    }
                    // Re-aplicar filtro si había texto escrito
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

        // Filtro en tiempo real al escribir
        txtBusqueda.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { filtrar(); }
        });

        // Filtro al cambiar el combo
        cmbFiltroEstado.addActionListener(e -> filtrar());

        // LÓGICA DE NAVEGACIÓN INTELIGENTE
        btnVerDetalle.addActionListener(e -> {
            int row = tblTramites.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Seleccione un trámite de la lista para ver detalles.");
                return;
            }

            // Obtener datos de la fila seleccionada (convertir índice modelo por si está filtrado)
            int modelRow = tblTramites.convertRowIndexToModel(row);
            String cedula = (String) tableModel.getValueAt(modelRow, 1);
            String estado = (String) tableModel.getValueAt(modelRow, 4);

            switch(estado) {
                case "PENDIENTE":
                    // Flujo: Verificar Requisitos
                    JOptionPane.showMessageDialog(this, "Trámite Pendiente: Abriendo Verificación de Requisitos...");
                    new VerificacionRequisitos().setVisible(true);
                    break;

                case "EN_EXAMENES":
                    // Flujo: Registrar Notas
                    JOptionPane.showMessageDialog(this, "Trámite en Exámenes: Abriendo Registro de Calificaciones...");
                    new RegistroExamenes().setVisible(true);
                    break;

                case "APROBADO":
                    // Flujo: Generar la Licencia (Detalle completo)
                    new DetalleTramite(cedula).setVisible(true);
                    break;

                case "LICENCIA_EMITIDA":
                    // Flujo: Ver Licencia ya emitida
                    int op = JOptionPane.showConfirmDialog(this,
                            "Licencia ya emitida. ¿Desea visualizar el documento?",
                            "Documento Listo", JOptionPane.YES_NO_OPTION);
                    if(op == JOptionPane.YES_OPTION) {
                        new GenerarLicencia(cedula).setVisible(true);
                    }
                    break;

                case "REPROBADO":
                    JOptionPane.showMessageDialog(this, "Este trámite fue REPROBADO. No se pueden realizar más acciones.");
                    break;

                default:
                    new DetalleTramite(cedula).setVisible(true);
            }
        });
    }

    private void filtrar() {
        String texto = txtBusqueda.getText().trim().toLowerCase();
        String estadoSeleccionado = (String) cmbFiltroEstado.getSelectedItem();

        if (estadoSeleccionado == null) return;

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String cedula = entry.getStringValue(1).toLowerCase();
                String nombre = entry.getStringValue(2).toLowerCase();
                String estadoRow = entry.getStringValue(4);

                boolean coincideTexto = cedula.contains(texto) || nombre.contains(texto);
                boolean coincideEstado = estadoSeleccionado.equals("TODOS") || estadoRow.equals(estadoSeleccionado);

                return coincideTexto && coincideEstado;
            }
        };

        sorter.setRowFilter(rf);
    }
}