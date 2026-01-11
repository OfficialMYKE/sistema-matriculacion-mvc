package com.licencias.sistemalicenciasfx.model.entities;

public class Usuario {

    private Long id; // Cambiado a Long para coincidir con BIGINT de la BD
    private String cedula;
    private String nombres;
    private String apellidos;
    private String username;
    private String password;
    private String rol;    // Usaremos String para facilitar el mapeo con la BD y el ComboBox
    private String estado; // ACTIVO / INACTIVO
    private String email;

    // Constructor Vacío (OBLIGATORIO)
    public Usuario() {
    }

    // Constructor Completo
    public Usuario(Long id, String cedula, String nombres, String apellidos,
                   String username, String password, String rol, String estado, String email) {
        this.id = id;
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.estado = estado;
        this.email = email;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Método de utilidad para mostrar nombre completo en la interfaz si se requiere
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return nombres + " " + apellidos + " (" + rol + ")";
    }
}