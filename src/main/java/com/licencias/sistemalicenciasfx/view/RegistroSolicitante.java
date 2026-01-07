package com.licencias.sistemalicenciasfx.view;

import com.licencias.sistemalicenciasfx.service.SupabaseService;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RegistroSolicitante extends JFrame {

    private JPanel panelPrincipal;

    // Controles FOTO
    private JLabel lblFoto;
    private JButton btnSubirFoto;

    // Controles DATOS
    private JTextField txtCedula;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtEmail;
    private JTextField txtCelular;
    private JTextField txtDireccion;
    private JTextField txtFechaNacimiento; // NUEVO
    private JComboBox<String> cmbTipo;
    private JTextField txtFecha;

    // Botones
    private JButton btnGuardar;
    private JButton btnCancelar;

    private File archivoFotoSeleccionado;

    // SERVICIO SUPABASE
    private final SupabaseService supabaseService;

    // CONFIGURACIÓN ESTÉTICA
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138);
    private final Color COLOR_DANGER = new Color(220, 53, 69);
    
    // FORMATO DE FECHA
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // DIMENSIONES DE FOTO
    private static final int FOTO_ANCHO = 300;
    private static final int FOTO_ALTO = 400;

    public RegistroSolicitante() {
        // Inicializamos el servicio para guardar datos
        this.supabaseService = new SupabaseService();

        setContentPane(panelPrincipal);
        setTitle("Registro de Solicitante");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        personalizarUI();
        iniciarLogica();
    }

    private void personalizarUI() {
        // INPUTS
        estilizarInput(txtCedula);
        estilizarInput(txtNombres);
        estilizarInput(txtApellidos);
        estilizarInput(txtEmail);
        estilizarInput(txtCelular);
        estilizarInput(txtDireccion);
        
        estilizarInput(txtFechaNacimiento);
        agregarPlaceholder(txtFechaNacimiento, "dd/MM/yyyy"); // Ayuda visual

        estilizarInput(txtFecha);
        txtFecha.setBackground(new Color(233, 236, 239)); // Readonly

        cmbTipo.setBackground(COLOR_BG_INPUT);
        cmbTipo.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        cmbTipo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblFoto.setOpaque(true);
        lblFoto.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));

        pintarPlaceholderFoto();

        // BOTONES
        estilizarBoton(btnGuardar, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnSubirFoto, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnCancelar, COLOR_DANGER, Color.WHITE);
    }

    private void estilizarInput(JTextField campo) {
        campo.setOpaque(true);
        campo.setBackground(COLOR_BG_INPUT);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER_INPUT, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBackground(Color.WHITE);
                campo.setBorder(new CompoundBorder(
                        new LineBorder(COLOR_ACCENT, 2),
                        new EmptyBorder(9, 14, 9, 14)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                campo.setBackground(COLOR_BG_INPUT);
                campo.setBorder(new CompoundBorder(
                        new LineBorder(COLOR_BORDER_INPUT, 1),
                        new EmptyBorder(10, 15, 10, 15)
                ));
            }
        });
    }
    
    private void agregarPlaceholder(JTextField campo, String placeholder) {
        campo.setText(placeholder);
        campo.setForeground(Color.GRAY);
        
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(campo.getText().equals(placeholder)) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if(campo.getText().isEmpty()) {
                    campo.setText(placeholder);
                    campo.setForeground(Color.GRAY);
                }
            }
        });
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
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
    }

    private void pintarPlaceholderFoto() {
        BufferedImage img = new BufferedImage(FOTO_ANCHO, FOTO_ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Color colorInicio = new Color(240, 240, 240);
        Color colorFin = new Color(220, 220, 220);
        GradientPaint gradiente = new GradientPaint(0, 0, colorInicio, 0, FOTO_ALTO, colorFin);
        g2.setPaint(gradiente);
        g2.fillRect(0, 0, FOTO_ANCHO, FOTO_ALTO);

        g2.setColor(new Color(180, 180, 180));
        int headRadius = FOTO_ANCHO / 3;
        int headX = (FOTO_ANCHO - headRadius) / 2;
        int headY = FOTO_ALTO / 4;
        g2.fillOval(headX, headY, headRadius, headRadius);

        int bodyWidth = (int) (FOTO_ANCHO * 0.85);
        int bodyHeight = FOTO_ALTO / 2;
        int bodyX = (FOTO_ANCHO - bodyWidth) / 2;
        int bodyY = headY + headRadius + 15;
        g2.fillArc(bodyX, bodyY, bodyWidth, bodyHeight, 0, 180);

        g2.dispose();
        lblFoto.setIcon(new ImageIcon(img));
        lblFoto.setText("");
    }

    private void iniciarLogica() {
        txtFecha.setText(LocalDate.now().format(formatter));

        btnSubirFoto.addActionListener(e -> subirFoto());
        btnCancelar.addActionListener(e -> this.dispose());

        // LOGICA DE GUARDADO (SUPABASE)
        btnGuardar.addActionListener(e -> {
            // Validaciones Básicas
            if(txtCedula.getText().trim().isEmpty() || txtNombres.getText().trim().isEmpty() || txtFechaNacimiento.getText().equals("dd/MM/yyyy")) {
                JOptionPane.showMessageDialog(this, "Por favor, complete los campos obligatorios (*).", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // VALIDACIÓN DE EDAD (< 17 AÑOS)
            LocalDate fechaNacimiento;
            try {
                fechaNacimiento = LocalDate.parse(txtFechaNacimiento.getText(), formatter);
                
                // Calculamos edad
                int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
                
                if (edad < 17) {
                    JOptionPane.showMessageDialog(this, 
                        "NO SE PUEDE REGISTRAR:\nEl solicitante tiene " + edad + " años.\nLa edad mínima requerida es de 17 años.",
                        "Edad Insuficiente", JOptionPane.ERROR_MESSAGE);
                    return; // DETIENE EL PROCESO
                }
                
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato de fecha inválido.\nUse el formato: dd/MM/yyyy (ej: 25/12/2000)", "Error de Fecha", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // UI Feedback
            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Proceso en Segundo Plano (Thread)
            // Necesitamos una variable final efectiva para el thread
            final LocalDate fechaNacFinal = fechaNacimiento; 
            
            new Thread(() -> {
                String fotoUrl = null;

                // Subir Foto (Si existe)
                if(archivoFotoSeleccionado != null) {
                    fotoUrl = supabaseService.subirImagen(archivoFotoSeleccionado, txtCedula.getText().trim());
                }

                // Guardar en Base de Datos (Con fecha nacimiento)
                boolean exito = supabaseService.guardarSolicitante(
                        txtCedula.getText().trim(),
                        txtNombres.getText().trim(),
                        txtApellidos.getText().trim(),
                        txtEmail.getText().trim(),
                        txtCelular.getText().trim(),
                        txtDireccion.getText().trim(),
                        (String) cmbTipo.getSelectedItem(),
                        fechaNacFinal, // Pasamos la fecha
                        fotoUrl
                );

                SwingUtilities.invokeLater(() -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar Solicitante");
                    setCursor(Cursor.getDefaultCursor());

                    if(exito) {
                        JOptionPane.showMessageDialog(this, "¡Solicitante registrado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        });
    }

    private void subirFoto() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar Foto de Perfil");
        fc.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "png"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoFotoSeleccionado = fc.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(archivoFotoSeleccionado);
                if(img != null) {
                    Image s = img.getScaledInstance(FOTO_ANCHO, FOTO_ALTO, Image.SCALE_SMOOTH);
                    lblFoto.setIcon(new ImageIcon(s));
                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen seleccionada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
