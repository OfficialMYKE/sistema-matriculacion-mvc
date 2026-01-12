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
import org.bytedeco.opencv.opencv_core.Mat;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import com.github.sarxos.webcam.Webcam;

import org.bytedeco.javacpp.Loader;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import javax.imageio.ImageIO;

public class RegistroSolicitante extends JFrame {

    private JPanel panelPrincipal;

    // Controles FOTO
    private JLabel lblFoto;
    private JButton btnSubirFoto;

    // Tomar Foto
    private JButton btnTomarFoto;


    // Controles DATOS
    private JTextField txtCedula;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtEmail;
    private JTextField txtCelular;
    private JTextField txtDireccion;
    private JTextField txtFechaNacimiento;
    private JComboBox<String> cmbTipo;
    private JTextField txtFecha;

    // NUEVOS CONTROLES
    private JComboBox<String> cmbOrganos;
    private JComboBox<String> cmbSangre;

    // Botones
    private JButton btnGuardar;
    private JButton btnRegresar;
    private JButton btnLimpiar;

    private File archivoFotoSeleccionado;
    private final SupabaseService supabaseService;

    // CONFIGURACIÓN ESTÉTICA
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138);
    private final Color COLOR_DANGER = new Color(220, 53, 69);

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int FOTO_ANCHO = 300;
    private static final int FOTO_ALTO = 400;

    public RegistroSolicitante() {
        this.supabaseService = new SupabaseService();

        setContentPane(panelPrincipal);
        setTitle("Registro de Solicitante");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        cargarDatosCombos();
        personalizarUI();
        iniciarLogica();


    }

    private void cargarDatosCombos() {
        if (cmbSangre != null && cmbSangre.getItemCount() == 0) {
            String[] tipos = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
            for (String t : tipos) cmbSangre.addItem(t);
        }
        if (cmbOrganos != null && cmbOrganos.getItemCount() == 0) {
            cmbOrganos.addItem("SI");
            cmbOrganos.addItem("NO");
        }
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
        agregarPlaceholder(txtFechaNacimiento, "dd/MM/yyyy");

        estilizarInput(txtFecha);
        txtFecha.setBackground(new Color(233, 236, 239));

        estilizarComboBox(cmbTipo);
        estilizarComboBox(cmbSangre);
        estilizarComboBox(cmbOrganos);

        // FOTO
        if (lblFoto != null) {
            lblFoto.setOpaque(true);
            lblFoto.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
            pintarPlaceholderFoto();
        }

        // --- BOTONES ---

        // 1. Botón Guardar (Azul)
        estilizarBoton(btnGuardar, COLOR_ACCENT, Color.WHITE);

        // 2. Botón Subir Foto (Azul)
        estilizarBoton(btnSubirFoto, COLOR_ACCENT, Color.WHITE);

        // 3. Botón Regresar (AHORA IDENTICO A VERIFICACION REQUISITOS)
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));

        // 4. Botón Limpiar (Gris)
        if(btnLimpiar != null) estilizarBoton(btnLimpiar, Color.GRAY, Color.WHITE);
    }

    // --- MÉTODOS DE ESTILO ---

    /**
     * Este método ha sido actualizado para ser idéntico al de VerificacionRequisitos.
     * Incluye paintText para asegurar colores correctos.
     */
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
                // Forzamos el color del texto definido en el botón
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

    private void estilizarInput(JTextField campo) {
        if(campo == null) return;
        campo.setOpaque(true);
        campo.setBackground(COLOR_BG_INPUT);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER_INPUT, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        campo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                campo.setBackground(Color.WHITE);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_ACCENT, 2), new EmptyBorder(9, 14, 9, 14)));
            }
            public void focusLost(FocusEvent e) {
                campo.setBackground(COLOR_BG_INPUT);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(10, 15, 10, 15)));
            }
        });
    }

    private void estilizarComboBox(JComboBox<String> combo) {
        if(combo == null) return;
        combo.setBackground(COLOR_BG_INPUT);
        combo.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void agregarPlaceholder(JTextField campo, String placeholder) {
        if(campo == null) return;
        campo.setText(placeholder);
        campo.setForeground(Color.GRAY);
        campo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if(campo.getText().equals(placeholder)) { campo.setText(""); campo.setForeground(Color.BLACK); }
            }
            public void focusLost(FocusEvent e) {
                if(campo.getText().isEmpty()) { campo.setText(placeholder); campo.setForeground(Color.GRAY); }
            }
        });
    }

    private void pintarPlaceholderFoto() {
        if(lblFoto == null) return;
        BufferedImage img = new BufferedImage(FOTO_ANCHO, FOTO_ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradiente = new GradientPaint(0, 0, new Color(240, 240, 240), 0, FOTO_ALTO, new Color(220, 220, 220));
        g2.setPaint(gradiente);
        g2.fillRect(0, 0, FOTO_ANCHO, FOTO_ALTO);
        g2.setColor(new Color(180, 180, 180));
        int headRadius = FOTO_ANCHO / 3;
        g2.fillOval((FOTO_ANCHO - headRadius) / 2, FOTO_ALTO / 4, headRadius, headRadius);
        int bodyWidth = (int) (FOTO_ANCHO * 0.85);
        g2.fillArc((FOTO_ANCHO - bodyWidth) / 2, (FOTO_ALTO / 4) + headRadius + 15, bodyWidth, FOTO_ALTO / 2, 0, 180);
        g2.dispose();
        lblFoto.setIcon(new ImageIcon(img));
        lblFoto.setText("");
    }

    private void iniciarLogica() {
        if(txtFecha != null) txtFecha.setText(LocalDate.now().format(formatter));

        if(btnSubirFoto != null) btnSubirFoto.addActionListener(e -> subirFoto());
        if(btnRegresar != null) btnRegresar.addActionListener(e -> this.dispose());
        if(btnLimpiar != null) btnLimpiar.addActionListener(e -> limpiarFormulario());
        if(btnGuardar != null) btnGuardar.addActionListener(e -> guardarDatos());
        if (btnTomarFoto != null) btnTomarFoto.addActionListener(e -> abrirVentanaCamara());

    }

    private void limpiarFormulario() {
        txtCedula.setText(""); txtNombres.setText(""); txtApellidos.setText("");
        txtEmail.setText(""); txtCelular.setText(""); txtDireccion.setText("");
        txtFechaNacimiento.setText("dd/MM/yyyy"); txtFechaNacimiento.setForeground(Color.GRAY);
        if(cmbTipo != null) cmbTipo.setSelectedIndex(0);
        if(cmbSangre != null) cmbSangre.setSelectedIndex(0);
        if(cmbOrganos != null) cmbOrganos.setSelectedIndex(0);
        archivoFotoSeleccionado = null;
        pintarPlaceholderFoto();
    }

    private void guardarDatos() {

        if (!validarFormulario()) {
            return;
        }

        if(txtCedula.getText().trim().isEmpty() || txtNombres.getText().trim().isEmpty() || txtFechaNacimiento.getText().equals("dd/MM/yyyy")) {
            JOptionPane.showMessageDialog(this, "Complete los campos obligatorios (*).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate fechaNacimiento;
        try {
            fechaNacimiento = LocalDate.parse(txtFechaNacimiento.getText(), formatter);
            if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 17) {
                JOptionPane.showMessageDialog(this, "Edad mínima requerida: 17 años.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Fecha inválida (dd/MM/yyyy).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        final LocalDate fechaNacFinal = fechaNacimiento;
        final String tipoSangre = (String) cmbSangre.getSelectedItem();
        final boolean esDonante = "SI".equalsIgnoreCase((String) cmbOrganos.getSelectedItem());

        new Thread(() -> {
            try {
                String fotoUrl = null;
                if(archivoFotoSeleccionado != null) {
                    fotoUrl = supabaseService.subirImagen(archivoFotoSeleccionado, txtCedula.getText().trim());
                }

                boolean exito = supabaseService.guardarSolicitante(
                        txtCedula.getText().trim(), txtNombres.getText().trim(), txtApellidos.getText().trim(),
                        txtEmail.getText().trim(), txtCelular.getText().trim(), txtDireccion.getText().trim(),
                        (String) cmbTipo.getSelectedItem(), tipoSangre, esDonante, fechaNacFinal, fotoUrl,
                        txtCedula.getText().trim(), true
                );

                SwingUtilities.invokeLater(() -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar Solicitante");
                    setCursor(Cursor.getDefaultCursor());
                    if(exito) {
                        JOptionPane.showMessageDialog(this, "Registrado exitosamente.");
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                    setCursor(Cursor.getDefaultCursor());
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void abrirVentanaCamara() {
        JFrame camFrame = new JFrame("Cámara");
        camFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        camFrame.setSize(640, 480);
        camFrame.setLayout(new BorderLayout());

        // Webcam
        Webcam webcam = Webcam.getDefault();
        webcam.open();

        // Panel para mostrar la imagen en vivo
        JLabel lblPreview = new JLabel();
        camFrame.add(lblPreview, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCapturar = new JButton("Capturar");
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnLimpiar = new JButton("Limpiar");
        panelBotones.add(btnCapturar);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnLimpiar);
        camFrame.add(panelBotones, BorderLayout.SOUTH);

        // Hilo para actualizar la vista previa en vivo
        Thread previewThread = new Thread(() -> {
            while (webcam.isOpen()) {
                ImageIcon icon = new ImageIcon(webcam.getImage());
                lblPreview.setIcon(icon);
                try {
                    Thread.sleep(100); // refresco cada 100ms
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        previewThread.start();

        // Acción Capturar
        btnCapturar.addActionListener(e -> {
            try {
                // Tomar imagen actual
                java.awt.Image img = webcam.getImage();
                lblFoto.setIcon(new ImageIcon(img.getScaledInstance(FOTO_ANCHO, FOTO_ALTO, Image.SCALE_SMOOTH)));

                // Guardar temporalmente
                File archivoTemp = new File("foto_temp.png");
                javax.imageio.ImageIO.write((java.awt.image.RenderedImage) img, "png", archivoTemp);
                archivoFotoSeleccionado = archivoTemp;

                JOptionPane.showMessageDialog(camFrame, "Foto capturada correctamente.");
                camFrame.dispose();
                webcam.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(camFrame, "Error al capturar la foto: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción Cancelar
        btnCancelar.addActionListener(e -> {
            camFrame.dispose();
            webcam.close();
        });

        // Acción Limpiar
        btnLimpiar.addActionListener(e -> {
            archivoFotoSeleccionado = null;
            pintarPlaceholderFoto(); // vuelve al placeholder en tu interfaz
            JOptionPane.showMessageDialog(camFrame, "Foto eliminada, puede intentar de nuevo.");
        });

        camFrame.setVisible(true);
    }



    private void subirFoto() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoFotoSeleccionado = fc.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(archivoFotoSeleccionado);
                if(img != null) lblFoto.setIcon(new ImageIcon(img.getScaledInstance(FOTO_ANCHO, FOTO_ALTO, Image.SCALE_SMOOTH)));
            } catch(Exception ex) {}
        }
    }





    private void tomarFoto() {
        Webcam webcam = Webcam.getDefault();

        if (webcam != null) {
            webcam.open();

            // Capturar imagen
            ImageIcon icon = new ImageIcon(webcam.getImage());

            // Mostrar en el JLabel
            lblFoto.setIcon(new ImageIcon(
                    icon.getImage().getScaledInstance(FOTO_ANCHO, FOTO_ALTO, Image.SCALE_SMOOTH)
            ));

            try {
                // Guardar temporalmente
                File archivoTemp = new File("foto_temp.png");
                javax.imageio.ImageIO.write(
                        (java.awt.image.RenderedImage) webcam.getImage(),
                        "png",
                        archivoTemp
                );
                archivoFotoSeleccionado = archivoTemp;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar la foto: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

            webcam.close();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se detectó ninguna cámara conectada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }



    /**
     * Convierte un objeto Mat de OpenCV a BufferedImage para usar en Swing.
     */

    public BufferedImage matToBufferedImage(Mat mat) {
        // Determinar el tipo de imagen
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        // Crear BufferedImage con las dimensiones del Mat
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);

        // Copiar los datos del Mat al BufferedImage
        mat.data().get(((DataBufferByte) image.getRaster().getDataBuffer()).getData());

        return image;
    }


    // Validar cedula
    private boolean validarCedulaEcuatoriana(String cedula) {

        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }

        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || provincia > 24) {
            return false;
        }

        int tercerDigito = Character.getNumericValue(cedula.charAt(2));
        if (tercerDigito < 0 || tercerDigito > 5) {
            return false;
        }

        int suma = 0;

        for (int i = 0; i < 9; i++) {
            int digito = Character.getNumericValue(cedula.charAt(i));

            if (i % 2 == 0) { // posiciones impares (0,2,4,6,8)
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }

            suma += digito;
        }

        int verificadorCalculado = (10 - (suma % 10)) % 10;
        int verificadorReal = Character.getNumericValue(cedula.charAt(9));

        return verificadorCalculado == verificadorReal;
    }



    // Validar datos
     private boolean validarFormulario() {

         if (!validarCedulaEcuatoriana(txtCedula.getText().trim())) {
             mostrarError("La cédula ingresada no es válida en Ecuador.");
             return false;
         }


         if (!txtCedula.getText().matches("\\d{10}")) {
             mostrarError("La cédula debe tener 10 dígitos.");
             return false;
         }

         if (txtNombres.getText().trim().isEmpty()) {
             mostrarError("Los nombres son obligatorios.");
             return false;
         }

         if (txtApellidos.getText().trim().isEmpty()) {
             mostrarError("Los apellidos son obligatorios.");
             return false;
         }

         if (!txtEmail.getText().trim().isEmpty() &&
                 !txtEmail.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
             mostrarError("Correo electrónico inválido.");
             return false;
         }

         if (!txtCelular.getText().trim().isEmpty() &&
                 !txtCelular.getText().matches("\\d{10}")) {
             mostrarError("El celular debe tener 10 dígitos.");
             return false;
         }

         if (txtFechaNacimiento.getText().equals("dd/MM/yyyy")) {
             mostrarError("Ingrese la fecha de nacimiento.");
             return false;
         }

         try {
             LocalDate fecha = LocalDate.parse(txtFechaNacimiento.getText(), formatter);
             if (Period.between(fecha, LocalDate.now()).getYears() < 17) {
                 mostrarError("Edad mínima requerida: 17 años.");
                 return false;
             }
         } catch (DateTimeParseException e) {
             mostrarError("Formato de fecha inválido (dd/MM/yyyy).");
             return false;
         }

         return true;
     }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }



}