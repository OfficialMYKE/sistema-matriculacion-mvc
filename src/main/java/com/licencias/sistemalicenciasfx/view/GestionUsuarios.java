package com.licencias.sistemalicenciasfx.view;

import com.licencias.sistemalicenciasfx.model.entities.Usuario;
import com.licencias.sistemalicenciasfx.service.SupabaseService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class GestionUsuarios extends JFrame {

    // --- COLORES CORPORATIVOS ---
    private final Color COLOR_HEADER = new Color(30, 58, 138); // Azul EPN
    private final Color COLOR_BG_PANEL = Color.WHITE;
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER = new Color(200, 200, 200);

    // Colores de Botones
    private final Color COLOR_BTN_PRIMARY = new Color(30, 58, 138); // Azul
    private final Color COLOR_BTN_WARNING = new Color(255, 193, 7); // Amarillo
    private final Color COLOR_BTN_DANGER = new Color(220, 53, 69);  // Rojo
    private final Color COLOR_BTN_GRAY = new Color(108, 117, 125);  // Gris
    private final Color COLOR_BTN_DARK = new Color(52, 58, 64);     // Gris Oscuro

    // --- COMPONENTES UI ---
    private JPanel panelPrincipal;

    // Inputs
    private JTextField txtCedula, txtNombres, txtApellidos, txtEmail, txtUsername, txtBuscar;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRol, cmbEstado;

    // Botones
    private JButton btnGuardar, btnActualizar, btnEstado, btnLimpiar, btnBuscar, btnRegresar;

    // Tabla
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    // Lógica
    private final SupabaseService supabaseService;

    public GestionUsuarios() {
        this.supabaseService = new SupabaseService();

        // Configuración Ventana
        setTitle("Gestión de Usuarios - EPN");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla Completa

        construirInterfaz();
        cargarTabla("");
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

        JLabel lblTitulo = new JLabel("Gestión de Usuarios (Administrador)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        panelPrincipal.add(pnlHeader, BorderLayout.NORTH);

        // 2. CONTENIDO (Split: Formulario Izq | Tabla Der)
        JPanel pnlContenido = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlContenido.setBackground(new Color(245, 247, 250)); // Fondo gris suave
        pnlContenido.setBorder(new EmptyBorder(20, 20, 20, 20));

        pnlContenido.add(construirPanelFormulario());
        pnlContenido.add(construirPanelTabla());

        panelPrincipal.add(pnlContenido, BorderLayout.CENTER);
    }

    private JPanel construirPanelFormulario() {
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new CompoundBorder(
                new LineBorder(new Color(230,230,230), 1),
                new EmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Título del Formulario
        JLabel lblSub = new JLabel("Información del Usuario");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSub.setForeground(COLOR_HEADER);
        pnlForm.add(lblSub, gbc);

        gbc.gridy++;
        pnlForm.add(new JSeparator(), gbc);

        // Campos
        gbc.gridy++; pnlForm.add(crearLabel("Cédula de Identidad"), gbc);
        gbc.gridy++; txtCedula = crearInput(); pnlForm.add(txtCedula, gbc);

        gbc.gridy++; pnlForm.add(crearLabel("Nombres"), gbc);
        gbc.gridy++; txtNombres = crearInput(); pnlForm.add(txtNombres, gbc);

        gbc.gridy++; pnlForm.add(crearLabel("Apellidos"), gbc);
        gbc.gridy++; txtApellidos = crearInput(); pnlForm.add(txtApellidos, gbc);

        gbc.gridy++; pnlForm.add(crearLabel("Correo Electrónico"), gbc);
        gbc.gridy++; txtEmail = crearInput(); pnlForm.add(txtEmail, gbc);

        // Fila Doble
        JPanel pnlRow = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlRow.setOpaque(false);

        JPanel pnlUser = new JPanel(new BorderLayout()); pnlUser.setOpaque(false);
        pnlUser.add(crearLabel("Username"), BorderLayout.NORTH);
        txtUsername = crearInput();
        pnlUser.add(txtUsername, BorderLayout.CENTER);

        JPanel pnlPass = new JPanel(new BorderLayout()); pnlPass.setOpaque(false);
        pnlPass.add(crearLabel("Contraseña"), BorderLayout.NORTH);
        txtPassword = new JPasswordField(); estilizarInput(txtPassword);
        pnlPass.add(txtPassword, BorderLayout.CENTER);

        pnlRow.add(pnlUser);
        pnlRow.add(pnlPass);
        gbc.gridy++; pnlForm.add(pnlRow, gbc);

        // Fila Doble (Rol / Estado)
        JPanel pnlRow2 = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlRow2.setOpaque(false);

        JPanel pnlRol = new JPanel(new BorderLayout()); pnlRol.setOpaque(false);
        pnlRol.add(crearLabel("Rol Asignado"), BorderLayout.NORTH);

        // --- AQUÍ ESTÁ EL CAMBIO PRINCIPAL ---
        // Se han limitado los roles a solo los dos requeridos:
        // ADMIN (Administrador) y ANALISTA (Analista de Matriculación)
        cmbRol = new JComboBox<>(new String[]{"ADMIN", "ANALISTA"});
        estilizarCombo(cmbRol);
        pnlRol.add(cmbRol, BorderLayout.CENTER);

        JPanel pnlEstado = new JPanel(new BorderLayout()); pnlEstado.setOpaque(false);
        pnlEstado.add(crearLabel("Estado Actual"), BorderLayout.NORTH);
        cmbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});
        cmbEstado.setEnabled(false);
        estilizarCombo(cmbEstado);
        pnlEstado.add(cmbEstado, BorderLayout.CENTER);

        pnlRow2.add(pnlRol);
        pnlRow2.add(pnlEstado);
        gbc.gridy++; pnlForm.add(pnlRow2, gbc);

        // --- BOTONES DE ACCIÓN (CRUD) ---
        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 0, 0);
        JPanel pnlBotones = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlBotones.setOpaque(false);
        pnlBotones.setPreferredSize(new Dimension(0, 45));

        btnLimpiar = new JButton("Limpiar");
        btnEstado = new JButton("Desactivar");
        btnActualizar = new JButton("Actualizar");
        btnGuardar = new JButton("Guardar");

        // Estilos
        estilizarBoton(btnLimpiar, COLOR_BTN_GRAY, Color.WHITE);
        estilizarBoton(btnEstado, COLOR_BTN_DANGER, Color.WHITE);
        estilizarBoton(btnActualizar, COLOR_BTN_WARNING, Color.WHITE);
        estilizarBoton(btnGuardar, COLOR_BTN_PRIMARY, Color.WHITE);

        btnActualizar.setEnabled(false);
        btnEstado.setEnabled(false);

        btnLimpiar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnEstado.addActionListener(e -> cambiarEstado());

        pnlBotones.add(btnLimpiar);
        pnlBotones.add(btnEstado);
        pnlBotones.add(btnActualizar);
        pnlBotones.add(btnGuardar);

        pnlForm.add(pnlBotones, gbc);

        // --- BOTÓN REGRESAR ---
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);

        btnRegresar = new JButton("Regresar al Menú");
        btnRegresar.setPreferredSize(new Dimension(0, 45));
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER, 1));

        btnRegresar.addActionListener(e -> this.dispose());

        pnlForm.add(btnRegresar, gbc);

        // Espaciador final
        gbc.gridy++; gbc.weighty = 1.0;
        pnlForm.add(Box.createVerticalGlue(), gbc);

        return pnlForm;
    }

    private JPanel construirPanelTabla() {
        JPanel pnlTabla = new JPanel(new BorderLayout(0, 15));
        pnlTabla.setOpaque(false);

        // Barra de Búsqueda
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(new JLabel("Buscar usuario:"), BorderLayout.WEST);

        txtBuscar = crearInput();
        pnlSearch.add(txtBuscar, BorderLayout.CENTER);

        btnBuscar = new JButton("Buscar");
        btnBuscar.setPreferredSize(new Dimension(100, 35));
        estilizarBoton(btnBuscar, COLOR_BTN_PRIMARY, Color.WHITE);
        btnBuscar.addActionListener(e -> cargarTabla(txtBuscar.getText()));
        pnlSearch.add(btnBuscar, BorderLayout.EAST);

        pnlTabla.add(pnlSearch, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"Cédula", "Nombres", "Usuario", "Rol", "Estado", "Email"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaUsuarios = new JTable(modeloTabla);
        estilizarTabla(tablaUsuarios);

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { seleccionarUsuario(); }
        });

        JScrollPane scroll = new JScrollPane(tablaUsuarios);
        scroll.setBorder(new LineBorder(COLOR_BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        pnlTabla.add(scroll, BorderLayout.CENTER);

        return pnlTabla;
    }

    // --- LÓGICA DE NEGOCIO ---
    private void guardarUsuario() {
        if(validarCampos()) {
            boolean ok = supabaseService.guardarUsuario(
                    txtCedula.getText(), txtNombres.getText(), txtApellidos.getText(),
                    txtUsername.getText(), new String(txtPassword.getPassword()),
                    (String)cmbRol.getSelectedItem(), txtEmail.getText()
            );
            if(ok) {
                JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.");
                limpiar();
                cargarTabla("");
            } else {
                JOptionPane.showMessageDialog(this, "Error: Cédula o Usuario ya existen.");
            }
        }
    }

    private void actualizarUsuario() {
        boolean ok = supabaseService.actualizarUsuario(
                txtCedula.getText(), txtNombres.getText(), txtApellidos.getText(),
                txtUsername.getText(), new String(txtPassword.getPassword()),
                (String)cmbRol.getSelectedItem(), txtEmail.getText()
        );
        if(ok) {
            JOptionPane.showMessageDialog(this, "Datos actualizados.");
            limpiar();
            cargarTabla("");
        }
    }

    private void cambiarEstado() {
        String actual = (String) cmbEstado.getSelectedItem();
        String nuevo = actual.equals("ACTIVO") ? "INACTIVO" : "ACTIVO";
        if(supabaseService.cambiarEstadoUsuario(txtCedula.getText(), nuevo)) {
            JOptionPane.showMessageDialog(this, "Estado cambiado a " + nuevo);
            limpiar();
            cargarTabla("");
        }
    }

    private void cargarTabla(String filtro) {
        modeloTabla.setRowCount(0);
        List<Usuario> usuarios = supabaseService.buscarUsuarios(filtro);
        for(Usuario u : usuarios) {
            modeloTabla.addRow(new Object[]{
                    u.getCedula(), u.getNombreCompleto(), u.getUsername(),
                    u.getRol(), u.getEstado(), u.getEmail()
            });
        }
    }

    private void seleccionarUsuario() {
        int row = tablaUsuarios.getSelectedRow();
        if(row >= 0) {
            txtCedula.setText(modeloTabla.getValueAt(row, 0).toString());
            txtCedula.setEditable(false);

            String nombreCompleto = modeloTabla.getValueAt(row, 1).toString();
            txtNombres.setText(nombreCompleto.split(" ")[0]);
            try { txtApellidos.setText(nombreCompleto.split(" ")[1]); } catch(Exception e) { txtApellidos.setText(""); }

            txtUsername.setText(modeloTabla.getValueAt(row, 2).toString());
            cmbRol.setSelectedItem(modeloTabla.getValueAt(row, 3).toString());
            cmbEstado.setSelectedItem(modeloTabla.getValueAt(row, 4).toString());
            txtEmail.setText(modeloTabla.getValueAt(row, 5).toString());
            txtPassword.setText("");

            btnGuardar.setEnabled(false);
            btnActualizar.setEnabled(true);
            btnEstado.setEnabled(true);
            btnEstado.setText(cmbEstado.getSelectedItem().equals("ACTIVO") ? "Desactivar" : "Activar");
        }
    }

    private void limpiar() {
        txtCedula.setText(""); txtCedula.setEditable(true);
        txtNombres.setText(""); txtApellidos.setText("");
        txtUsername.setText(""); txtEmail.setText("");
        txtPassword.setText("");
        cmbRol.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEstado.setEnabled(false);
        btnEstado.setText("Desactivar");
        tablaUsuarios.clearSelection();
    }

    private boolean validarCampos() {

        String cedula = txtCedula.getText().trim();
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (cedula.isEmpty() || nombres.isEmpty() || apellidos.isEmpty()
                || username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return false;
        }

        if (!validarCedulaEcuatoriana(cedula)) {
            JOptionPane.showMessageDialog(this, "La cédula ingresada no es válida en Ecuador.");
            return false;
        }

        if (!validarEmail(email)) {
            JOptionPane.showMessageDialog(this, "Ingrese un correo electrónico válido.");
            return false;
        }

        if (username.length() < 4) {
            JOptionPane.showMessageDialog(this, "El username debe tener al menos 4 caracteres.");
            return false;
        }

        // Contraseña SOLO obligatoria al crear
        if (btnGuardar.isEnabled() && password.length() < 6) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 6 caracteres.");
            return false;
        }

        return true;
    }



    // Validar email

    private boolean validarEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$");
    }


    // --- MÉTODOS DE ESTILO ---

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

    // --- ESTILO DE BOTÓN (MANUAL) ---
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

                // PINTAR TEXTO MANUALMENTE
                paintTextManual(g, c, ((AbstractButton)c).getText());
            }

            private void paintTextManual(Graphics g, JComponent c, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Usar el color definido (fg) para que funcione con Blanco y Gris
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


    private boolean validarCedulaEcuatoriana(String cedula) {

        if (cedula == null || !cedula.matches("\\d{10}")) return false;

        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || provincia > 24) return false;

        int tercer = Character.getNumericValue(cedula.charAt(2));
        if (tercer < 0 || tercer > 5) return false;

        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int dig = Character.getNumericValue(cedula.charAt(i));
            if (i % 2 == 0) {
                dig *= 2;
                if (dig > 9) dig -= 9;
            }
            suma += dig;
        }

        int verificador = (10 - (suma % 10)) % 10;
        return verificador == Character.getNumericValue(cedula.charAt(9));
    }

}