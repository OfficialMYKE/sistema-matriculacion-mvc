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

    private JPanel panelPrincipal;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JLabel lblFoto;
    private JLabel lblNombre;
    private JLabel lblCedula;
    private JLabel lblEdad;
    private JLabel lblEmail;
    private JLabel lblTipo;
    private JCheckBox chkCertificado;
    private JCheckBox chkPago;
    private JCheckBox chkMultas;
    private JTextArea txtObservaciones;
    private JButton btnAprobar;
    private JButton btnRechazar;
    private JButton btnRegresar;

    private final SupabaseService supabaseService;
    private Solicitante solicitanteActual;

    // CONFIGURACIÓN ESTÉTICA IDENTICA A REGISTRO
    private final Color COLOR_BG_INPUT = new Color(248, 249, 250);
    private final Color COLOR_BORDER_INPUT = new Color(200, 200, 200);
    private final Color COLOR_ACCENT = new Color(30, 58, 138); 
    private final Color COLOR_DANGER = new Color(220, 53, 69);

    public VerificacionRequisitos() {
        this.supabaseService = new SupabaseService();
        setContentPane(panelPrincipal);
        setTitle("Evaluación de Solicitantes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        personalizarUI();
        iniciarLogica();
    }

    private void personalizarUI() {
        // BUSQUEDA ESTILO REGISTRO
        txtBuscar.setOpaque(true);
        txtBuscar.setBackground(COLOR_BG_INPUT);
        txtBuscar.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(8, 12, 8, 12)));
        estilizarBoton(btnBuscar, Color.WHITE, Color.DARK_GRAY);
        btnBuscar.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));

        // FOTO
        lblFoto.setOpaque(true);
        lblFoto.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        pintarPlaceholderFoto();

        // TEXTAREA ESTILO REGISTRO
        txtObservaciones.setOpaque(true);
        txtObservaciones.setBackground(COLOR_BG_INPUT);
        txtObservaciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtObservaciones.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_INPUT, 1), new EmptyBorder(10, 10, 10, 10)));

        // BOTONES ACCIÓN IDENTICOS A REGISTRO
        estilizarBoton(btnAprobar, COLOR_ACCENT, Color.WHITE);
        estilizarBoton(btnRechazar, COLOR_DANGER, Color.WHITE);
        estilizarBoton(btnRegresar, Color.WHITE, Color.DARK_GRAY);
        btnRegresar.setBorder(new LineBorder(COLOR_BORDER_INPUT, 1));
        
        setControlesHabilitados(false);
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // FUENTE REGULAR
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
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20); // RADIO 20PX
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

    private void setControlesHabilitados(boolean enabled) {
        btnAprobar.setEnabled(enabled); btnRechazar.setEnabled(enabled);
        chkCertificado.setEnabled(enabled); chkPago.setEnabled(enabled); chkMultas.setEnabled(enabled);
        txtObservaciones.setEnabled(enabled);
    }

    private void iniciarLogica() {
        btnRegresar.addActionListener(e -> this.dispose());
        btnBuscar.addActionListener(e -> buscarSolicitantes());
        txtBuscar.addActionListener(e -> buscarSolicitantes());
        cargarUltimoPendiente();

        btnRechazar.addActionListener(e -> {
            if(txtObservaciones.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Ingrese el motivo de rechazo.");
                return;
            }
            if(JOptionPane.showConfirmDialog(this, "¿Rechazar trámite?", "Confirmar", 0) == 0) procesarEvaluacion("RECHAZADO");
        });

        btnAprobar.addActionListener(e -> {
            if(!chkCertificado.isSelected() || !chkPago.isSelected() || !chkMultas.isSelected()) {
                JOptionPane.showMessageDialog(this, "⚠️ Faltan requisitos.");
                return;
            }
            if(JOptionPane.showConfirmDialog(this, "¿Aprobar trámite?", "Confirmar", 0) == 0) procesarEvaluacion("EN_EXAMENES");
        });
    }

    private void cargarUltimoPendiente() {
        new Thread(() -> {
            this.solicitanteActual = supabaseService.obtenerSiguientePendiente();
            SwingUtilities.invokeLater(() -> mostrarSolicitante(solicitanteActual));
        }).start();
    }

    private void buscarSolicitantes() {
        String f = txtBuscar.getText().trim();
        if(f.isEmpty()) { cargarUltimoPendiente(); return; }
        new Thread(() -> {
            java.util.List<Solicitante> r = supabaseService.buscarPendientes(f);
            SwingUtilities.invokeLater(() -> {
                if(r.isEmpty()) JOptionPane.showMessageDialog(this, "No encontrado.");
                else if(r.size() == 1) mostrarSolicitante(r.get(0));
                else {
                    String[] o = r.stream().map(s -> s.getCedula() + " - " + s.getNombreCompleto()).toArray(String[]::new);
                    String e = (String) JOptionPane.showInputDialog(this, "Seleccione:", "Resultados", 3, null, o, o[0]);
                    if(e != null) r.stream().filter(s -> e.startsWith(s.getCedula())).findFirst().ifPresent(this::mostrarSolicitante);
                }
            });
        }).start();
    }

    private void mostrarSolicitante(Solicitante s) {
        this.solicitanteActual = s;
        if(s != null) {
            lblNombre.setText(s.getNombreCompleto()); lblCedula.setText("CI: " + s.getCedula());
            lblEdad.setText(Period.between(s.getFechaNacimiento(), LocalDate.now()).getYears() + " Años");
            lblEmail.setText(s.getEmail()); lblTipo.setText(s.getTipoLicencia());
            setControlesHabilitados(true); cargarImagen(s.getFotoUrl());
        } else {
            lblNombre.setText("Sin Pendientes"); lblCedula.setText("---"); pintarPlaceholderFoto(); setControlesHabilitados(false);
        }
        chkCertificado.setSelected(false); chkPago.setSelected(false); chkMultas.setSelected(false); txtObservaciones.setText("");
    }

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

    private void procesarEvaluacion(String st) {
        new Thread(() -> {
            if(supabaseService.actualizarEstadoSolicitante(solicitanteActual.getCedula(), st, txtObservaciones.getText())) {
                SwingUtilities.invokeLater(() -> { JOptionPane.showMessageDialog(this, "Éxito."); cargarUltimoPendiente(); });
            }
        }).start();
    }
}
