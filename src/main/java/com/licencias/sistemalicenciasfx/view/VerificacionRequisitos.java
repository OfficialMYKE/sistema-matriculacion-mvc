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
import java.time.LocalDate;
import java.time.Period;

public class VerificacionRequisitos extends JFrame {

    // Panel principal del formulario (vinculado al .form)
    private JPanel panelPrincipal;

    // Componentes de Búsqueda
    private JTextField txtBuscar;
    private JButton btnBuscar;

    // Etiquetas para mostrar información del solicitante
    private JLabel lblFoto;
    private JLabel lblNombre;
    private JLabel lblCedula;
    private JLabel lblEdad;
    private JLabel lblEmail;
    private JLabel lblTipo;

    // Checkboxes para validar requisitos físicos
    private JCheckBox chkCertificado;
    private JCheckBox chkPago;
    private JCheckBox chkMultas;

    // Campo para escribir notas si se rechaza
    private JTextArea txtObservaciones;

    // Botones de acción
    private JButton btnAprobar;
    private JButton btnRechazar;
    private JButton btnRegresar;

    // Lógica y Datos
    private final SupabaseService supabaseService; // Conexión a BD
    private Solicitante solicitanteActual;         // Objeto que estamos revisando actualmente

    // COLORES CORPORATIVOS
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138); // Azul
    private final Color COLOR_DANGER = new Color(220, 53, 69); // Rojo

    public VerificacionRequisitos() {

        this.supabaseService = new SupabaseService();

        // Configuración de la ventana
        setContentPane(panelPrincipal);
        setTitle("Evaluación de Solicitantes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa

        // Aplicar estilos y cargar lógica
        personalizarUI();
        iniciarLogica();
    }
    //Estilo general
    private void personalizarUI() {
        txtBuscar.setOpaque(true);
        txtBuscar.setBackground(COLOR_BG_INPUT);
        txtBuscar.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(8, 12, 8, 12)));

        estilizarBoton(btnBuscar, Color.WHITE, Color.DARK_GRAY);
        btnBuscar.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));

        lblFoto.setOpaque(true);
        lblFoto.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        pintarPlaceholderFoto();

        txtObservaciones.setOpaque(true);
        txtObservaciones.setBackground(COLOR_BG_INPUT);
        txtObservaciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtObservaciones.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(10, 10, 10, 10)));

        estilizarBoton(btnAprobar, COLOR_ACCENT, Color.WHITE); // Azul
        estilizarBoton(btnRechazar, COLOR_DANGER, Color.WHITE); // Rojo

        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));

        setControlesHabilitados(false);
    }


    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Quitamos los bordes por defecto de Java Swing
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // UI Personalizada (Overriding paint)
        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Antialiasing para que no se vea pixelado
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                g2.dispose();

                // Llamamos al padre para que pinte lo demás
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
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
                btn.setForeground(fg);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.setForeground(fg);
            }
        });
    }

    private void pintarPlaceholderFoto() {
        BufferedImage img = new BufferedImage(300, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, new Color(240, 240, 240), 0, 400, new Color(220, 220, 220)));
        g2.fillRect(0, 0, 300, 400);

        g2.setColor(new Color(180, 180, 180));
        g2.fillOval(100, 80, 100, 100); // Cabeza
        g2.fillArc(50, 200, 200, 180, 0, 180); // Cuerpo
        g2.dispose();

        lblFoto.setIcon(new ImageIcon(img));
    }

    private void setControlesHabilitados(boolean enabled) {
        btnAprobar.setEnabled(enabled);
        btnRechazar.setEnabled(enabled);
        chkCertificado.setEnabled(enabled);
        chkPago.setEnabled(enabled);
        chkMultas.setEnabled(enabled);
        txtObservaciones.setEnabled(enabled);
    }

    private void iniciarLogica() {
        btnRegresar.addActionListener(e -> this.dispose());

        btnBuscar.addActionListener(e -> buscarSolicitantes());
        txtBuscar.addActionListener(e -> buscarSolicitantes()); // Al dar Enter

        cargarUltimoPendiente();

        // Lógica RECHAZAR
        btnRechazar.addActionListener(e -> {
            if(txtObservaciones.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Ingrese el motivo de rechazo en Observaciones.");
                return;
            }
            if(JOptionPane.showConfirmDialog(this, "¿Rechazar trámite?", "Confirmar", 0) == 0)
                procesarEvaluacion("RECHAZADO");
        });

        // Lógica APROBAR
        btnAprobar.addActionListener(e -> {
            // Validar que todos los checks estén marcados
            if(!chkCertificado.isSelected() || !chkPago.isSelected() || !chkMultas.isSelected()) {
                JOptionPane.showMessageDialog(this, "⚠️ Faltan requisitos por verificar (Checkboxes).");
                return;
            }
            if(JOptionPane.showConfirmDialog(this, "¿Aprobar trámite?", "Confirmar", 0) == 0)
                procesarEvaluacion("EN_EXAMENES"); // Pasamos al siguiente estado
        });
    }

    //Busca automáticamente el siguiente trámite en estado PENDIENTE.
    private void cargarUltimoPendiente() {
        new Thread(() -> {
            this.solicitanteActual = supabaseService.obtenerSiguientePendiente();
            SwingUtilities.invokeLater(() -> mostrarSolicitante(solicitanteActual));
        }).start();
    }

    //Busca por cédula o nombre. Maneja múltiples resultados.
    private void buscarSolicitantes() {
        String f = txtBuscar.getText().trim();
        if(f.isEmpty()) { cargarUltimoPendiente(); return; } // Si está vacío, carga el siguiente

        new Thread(() -> {
            java.util.List<Solicitante> r = supabaseService.buscarPendientes(f);
            SwingUtilities.invokeLater(() -> {
                if(r.isEmpty()) JOptionPane.showMessageDialog(this, "No se encontraron trámites pendientes con ese criterio.");
                else if(r.size() == 1) mostrarSolicitante(r.get(0)); // Solo uno, lo mostramos directo
                else {
                    // Si hay varios, mostramos una lista para elegir
                    String[] o = r.stream().map(s -> s.getCedula() + " - " + s.getNombreCompleto()).toArray(String[]::new);
                    String e = (String) JOptionPane.showInputDialog(this, "Seleccione:", "Múltiples Resultados", 3, null, o, o[0]);
                    if(e != null) r.stream().filter(s -> e.startsWith(s.getCedula())).findFirst().ifPresent(this::mostrarSolicitante);
                }
            });
        }).start();
    }

    //Muestra los datos del objeto Solicitante en la interfaz gráfica.
    private void mostrarSolicitante(Solicitante s) {
        this.solicitanteActual = s;
        if(s != null) {
            lblNombre.setText(s.getNombreCompleto());
            lblCedula.setText("CI: " + s.getCedula());
            // Calculamos la edad exacta usando Period
            lblEdad.setText(Period.between(s.getFechaNacimiento(), LocalDate.now()).getYears() + " Años");
            lblEmail.setText(s.getEmail());
            lblTipo.setText(s.getTipoLicencia());

            setControlesHabilitados(true);
            cargarImagen(s.getFotoUrl()); // Cargar foto en hilo aparte
        } else {
            // Si no hay nadie, limpiamos todo
            lblNombre.setText("Sin Pendientes");
            lblCedula.setText("---");
            pintarPlaceholderFoto();
            setControlesHabilitados(false);
        }
        // Reseteamos los checks
        chkCertificado.setSelected(false);
        chkPago.setSelected(false);
        chkMultas.setSelected(false);
        txtObservaciones.setText("");
    }

    //Descarga la imagen desde la URL de Supabase en segundo plano.
    private void cargarImagen(String u) {
        if(u == null) { pintarPlaceholderFoto(); return; }
        new Thread(() -> {
            try {
                BufferedImage i = ImageIO.read(new URL(u));
                if(i != null) {
                    Image d = i.getScaledInstance(300, 400, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> lblFoto.setIcon(new ImageIcon(d)));
                }
            } catch (Exception e) { pintarPlaceholderFoto(); }
        }).start();
    }

    //Envía la actualización de estado (Aprobado/Rechazado) a la BD.
    private void procesarEvaluacion(String st) {
        new Thread(() -> {
            if(supabaseService.actualizarEstadoSolicitante(solicitanteActual.getCedula(), st, txtObservaciones.getText())) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Trámite actualizado con éxito.");
                    cargarUltimoPendiente(); // Cargamos el siguiente de la fila automáticamente
                });
            }
        }).start();
    }
}