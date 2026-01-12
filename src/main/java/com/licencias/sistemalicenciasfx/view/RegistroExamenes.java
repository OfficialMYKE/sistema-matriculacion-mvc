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

public class RegistroExamenes extends JFrame {

    private JPanel panelPrincipal;
    private JLabel lblFoto;
    private JLabel lblNombre;
    private JLabel lblCedula;
    private JTextField txtNotaTeorica;
    private JTextField txtNotaPractica;
    private JButton btnGuardar;
    private JButton btnRegresar;
    private JTextField textField1; // Campo de búsqueda
    private JButton buscarButton;

    private final SupabaseService supabaseService;
    private Solicitante solicitanteActual;

    // CONFIGURACIÓN ESTÉTICA
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138); // Azul
    private final Color COLOR_DANGER = new Color(220, 53, 69);  // Rojo

    public RegistroExamenes() {
        this.supabaseService = new SupabaseService();
        setContentPane(panelPrincipal);
        setTitle("Registro de Exámenes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        personalizarUI();
        iniciarLogica();
    }

    private void personalizarUI() {
        // INPUTS ESTILO REGISTRO
        estilizarInput(txtNotaTeorica);
        estilizarInput(txtNotaPractica);
        estilizarInput(textField1); // Estilo también para la búsqueda

        // FOTO
        if (lblFoto != null) {
            lblFoto.setOpaque(true);
            lblFoto.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
            pintarPlaceholderFoto();
        }

        // --- BOTONES ---
        estilizarBoton(btnGuardar, COLOR_ACCENT, Color.WHITE);

        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        if(btnRegresar != null) btnRegresar.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));

        estilizarBoton(buscarButton, COLOR_BG_INPUT, Color.BLACK);
    }

    private void estilizarInput(JTextField campo) {
        if (campo == null) return;
        campo.setOpaque(true);
        campo.setBackground(COLOR_BG_INPUT);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(10, 15, 10, 15)));

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBackground(Color.WHITE);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_ACCENT, 2), new EmptyBorder(9, 14, 9, 14)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                campo.setBackground(COLOR_BG_INPUT);
                campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(10, 15, 10, 15)));
            }
        });
    }

    // --- MÉTODO DE ESTILO DE BOTÓN ROBUSTO ---
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

                // Borde suave si es blanco
                if (bg.equals(Color.WHITE)) {
                    g2.setColor(COLOR_BORDER_INPUT);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, 20, 20);
                }

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

    private void pintarPlaceholderFoto() {
        if(lblFoto == null) return;
        BufferedImage img = new BufferedImage(300, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(240, 240, 240), 0, 400, new Color(220, 220, 220)));
        g2.fillRect(0, 0, 300, 400);
        g2.setColor(new Color(180, 180, 180));
        g2.fillOval(100, 80, 100, 100);
        g2.fillArc(50, 200, 200, 180, 0, 180);
        g2.dispose();
        lblFoto.setIcon(new ImageIcon(img));
    }

    private void iniciarLogica() {
        if(btnRegresar != null) btnRegresar.addActionListener(e -> this.dispose());

        // Cargar inicial
        cargarSiguientePostulante();

        // Búsqueda
        if(buscarButton != null) buscarButton.addActionListener(e -> buscarPostulante());
        if(textField1 != null) textField1.addActionListener(e -> buscarPostulante()); // Enter

        // LÓGICA DE GUARDADO MEJORADA
        if(btnGuardar != null) btnGuardar.addActionListener(e -> {
            if(solicitanteActual == null) return;

            try {
                // LIMPIEZA: Reemplazar coma por punto para evitar error
                String valTeo = txtNotaTeorica.getText().replace(",", ".");
                String valPrac = txtNotaPractica.getText().replace(",", ".");

                // CONVERSIÓN
                double teo = Double.parseDouble(valTeo);
                double prac = Double.parseDouble(valPrac);

                // VALIDACIÓN DE RANGO
                if (teo < 0 || teo > 20 || prac < 0 || prac > 20) {
                    JOptionPane.showMessageDialog(this, "Las notas deben estar entre 0 y 20.");
                    return;
                }

                // LÓGICA DE NEGOCIO
                String nuevoEstado = (teo >= 14 && prac >= 14) ? "APROBADO" : "REPROBADO";

                // INTERFAZ DE ESPERA
                btnGuardar.setEnabled(false);
                btnGuardar.setText("Guardando...");

                new Thread(() -> {
                    // LLAMADA AL SERVICIO CORRECTO (Que actualiza columnas numéricas)
                    // Usamos registrarResultadosExamenes en lugar de actualizarEstadoSolicitante
                    boolean exito = supabaseService.registrarResultadosExamenes(
                            solicitanteActual.getCedula(),
                            teo,
                            prac,
                            nuevoEstado
                    );

                    SwingUtilities.invokeLater(() -> {
                        if(exito) {
                            JOptionPane.showMessageDialog(this, "Resultados guardados correctamente.\nEstado final: " + nuevoEstado);
                            cargarSiguientePostulante(); // Limpia y carga el siguiente
                        } else {
                            JOptionPane.showMessageDialog(this, "Error al guardar en base de datos.");
                            btnGuardar.setEnabled(true);
                            btnGuardar.setText("Guardar Notas");
                        }
                    });
                }).start();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos (use punto o coma).");
            }
        });
    }

    private void cargarSiguientePostulante() {
        new Thread(() -> {
            this.solicitanteActual = supabaseService.obtenerSiguienteParaExamen();
            SwingUtilities.invokeLater(() -> {
                if(btnGuardar != null) btnGuardar.setText("Guardar Notas");

                if (solicitanteActual != null) {
                    if(lblNombre != null) lblNombre.setText(solicitanteActual.getNombreCompleto());
                    if(lblCedula != null) lblCedula.setText("CI: " + solicitanteActual.getCedula());
                    cargarImagen(solicitanteActual.getFotoUrl());
                    if(btnGuardar != null) btnGuardar.setEnabled(true);
                } else {
                    if(lblNombre != null) lblNombre.setText("No hay postulantes pendientes");
                    if(lblCedula != null) lblCedula.setText("---");
                    pintarPlaceholderFoto();
                    if(btnGuardar != null) btnGuardar.setEnabled(false);
                }

                if(txtNotaTeorica != null) txtNotaTeorica.setText("");
                if(txtNotaPractica != null) txtNotaPractica.setText("");
            });
        }).start();
    }

    private void cargarImagen(String u) {
        if(u == null || u.isEmpty()) { pintarPlaceholderFoto(); return; }
        new Thread(() -> {
            try {
                BufferedImage i = ImageIO.read(new URL(u));
                if(i != null) {
                    Image d = i.getScaledInstance(300, 400, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> { if(lblFoto != null) lblFoto.setIcon(new ImageIcon(d)); });
                }
            } catch (Exception e) { pintarPlaceholderFoto(); }
        }).start();
    }

    private void buscarPostulante() {
        if(textField1 == null) return;
        String filtro = textField1.getText().trim();

        if (filtro.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese cédula o nombre.");
            return;
        }

        if(btnGuardar != null) btnGuardar.setEnabled(false);
        if(lblNombre != null) lblNombre.setText("Buscando...");
        if(lblCedula != null) lblCedula.setText("");

        new Thread(() -> {
            Solicitante s = supabaseService.buscarPostulanteParaExamen(filtro);

            SwingUtilities.invokeLater(() -> {
                if (s == null) {
                    JOptionPane.showMessageDialog(this, "No se encontraron postulantes para examen.");
                    limpiarVista();
                    return;
                }

                solicitanteActual = s;
                mostrarSolicitante(s);
            });
        }).start();
    }


    private void mostrarSolicitante(Solicitante s) {
        if(lblNombre != null) lblNombre.setText(s.getNombreCompleto());
        if(lblCedula != null) lblCedula.setText("CI: " + s.getCedula());

        if(txtNotaTeorica != null) txtNotaTeorica.setText("");
        if(txtNotaPractica != null) txtNotaPractica.setText("");

        if(btnGuardar != null) {
            btnGuardar.setText("Guardar Notas");
            btnGuardar.setEnabled(true);
        }

        cargarImagen(s.getFotoUrl());
    }

    private void limpiarVista() {
        solicitanteActual = null;

        if(lblNombre != null) lblNombre.setText("—");
        if(lblCedula != null) lblCedula.setText("—");

        if(txtNotaTeorica != null) txtNotaTeorica.setText("");
        if(txtNotaPractica != null) txtNotaPractica.setText("");
        if(textField1 != null) textField1.setText("");

        if(btnGuardar != null) {
            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardar Notas");
        }

        pintarPlaceholderFoto();
    }
}