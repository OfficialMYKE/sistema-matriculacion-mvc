package com.licencias.sistemalicenciasfx.model.entities;

import java.time.LocalDate;

public class Solicitante {
    private String cedula;
    private String nombres;
    private String apellidos;
    private String email;
    private String celular;
    private String direccion;
    private String tipoLicencia;
    private LocalDate fechaNacimiento;
    private String fotoUrl;
    private String estado;

    // --- NUEVOS CAMPOS ---
    private String tipoSangre;
    private boolean esDonante;

    // CONSTRUCTOR ACTUALIZADO
    public Solicitante(String cedula, String nombres, String apellidos, String email,
                       String celular, String direccion, String tipoLicencia,
                       LocalDate fechaNacimiento, String fotoUrl, String estado,
                       String tipoSangre, boolean esDonante) { // <--- Nuevos parámetros
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.celular = celular;
        this.direccion = direccion;
        this.tipoLicencia = tipoLicencia;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoUrl = fotoUrl;
        this.estado = estado;
        this.tipoSangre = tipoSangre;
        this.esDonante = esDonante;
    }

    // Getters existentes
    public String getCedula() { return cedula; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getCelular() { return celular; }
    public String getDireccion() { return direccion; }
    public String getTipoLicencia() { return tipoLicencia; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getFotoUrl() { return fotoUrl; }
    public String getEstado() { return estado; }

    // --- NUEVOS GETTERS ---
    public String getTipoSangre() { return tipoSangre; }
    public boolean isEsDonante() { return esDonante; }

    // Helper para nombre
    public String getNombreCompleto() { return nombres + " " + apellidos; }
}