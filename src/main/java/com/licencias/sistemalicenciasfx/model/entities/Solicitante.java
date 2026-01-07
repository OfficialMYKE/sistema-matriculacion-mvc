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

    public Solicitante(String cedula, String nombres, String apellidos, String email, String celular, String direccion, String tipoLicencia, LocalDate fechaNacimiento, String fotoUrl, String estado) {
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
    }

    // Getters
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

    // Helper para nombre
    public String getNombreCompleto() { return nombres + " " + apellidos; }
}