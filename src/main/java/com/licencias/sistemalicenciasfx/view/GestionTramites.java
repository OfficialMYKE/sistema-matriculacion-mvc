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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final SupabaseService supabaseService;

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
        cargarDatos();
    }

    private void personalizarUI() {
        estilizarInput(txtBusqueda);

        cmbFiltroEstado.setBackground(COLOR_BG_INPUT);
        cmbFiltroEstado.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        cmbFiltroEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // BOTONES CON TEXTO REGULAR Y COLOR BLANCO FORZADO
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
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Estilo REGULAR
        btn.setFocusPainted(false);
        btn.setBorderPainted(bg == Color.WHITE);
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
                g2.setColor(b.getForeground()); // Forzado de color
                g2.setFont(b.getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = textRect.x + (textRect.width - fm.stringWidth(text)) / 2;
                int y = textRect.y + fm.getAscent();

                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
                btn.setForeground(fg);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.setForeground(fg);
            }
        });
    }

    private void inicializarTabla() {
        String[] columnas = {"ID", "CÉDULA", "NOMBRE", "TIPO", "ESTADO"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTramites.setModel(tableModel);
        tblTramites.setRowHeight(35);
        tblTramites.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblTramites.setBackground(Color.WHITE);

        tblTramites.getTableHeader().setBackground(COLOR_ACCENT);
        tblTramites.getTableHeader().setForeground(Color.WHITE);
        tblTramites.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblTramites.getTableHeader().setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i : new int[]{0, 1, 3, 4}) {
            tblTramites.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void configurarFiltros() {
        String[] estados = {"TODOS", "PENDIENTE", "EN_EXAMENES", "APROBADO", "REPROBADO", "LICENCIA_EMITIDA"};
        for(String est : estados) cmbFiltroEstado.addItem(est);
    }

    private void cargarDatos() {
        new Thread(() -> {
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
            });
        }).start();
    }

    private void iniciarLogica() {
        btnRegresar.addActionListener(e -> this.dispose());
        btnActualizar.addActionListener(e -> cargarDatos());

        txtBusqueda.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { filtrar(); }
        });
        cmbFiltroEstado.addActionListener(e -> filtrar());

        btnVerDetalle.addActionListener(e -> {
            int row = tblTramites.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un trámite.");
                return;
            }
            String estado = (String) tblTramites.getValueAt(row, 4);
            if(estado.equals("PENDIENTE")) new VerificacionRequisitos().setVisible(true);
            else if(estado.equals("EN_EXAMENES")) new RegistroExamenes().setVisible(true);
            else JOptionPane.showMessageDialog(this, "Estado del trámite: " + estado);
        });
    }

    private void filtrar() {
        String txt = txtBusqueda.getText().toLowerCase();
        String est = cmbFiltroEstado.getSelectedItem().toString();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tblTramites.setRowSorter(sorter);

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String c = entry.getStringValue(1).toLowerCase();
                String n = entry.getStringValue(2).toLowerCase();
                String s = entry.getStringValue(4);
                return (c.contains(txt) || n.contains(txt)) && (est.equals("TODOS") || s.equals(est));
            }
        });
    }
}