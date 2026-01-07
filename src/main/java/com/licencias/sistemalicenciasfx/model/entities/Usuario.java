package com.licencias.sistemalicenciasfx.model.entities;

import com.licencias.sistemalicenciasfx.model.enums.Rol;

public class Usuario {

    private int id;
    private String username;
    private String password;
    private Rol rol;

    // Constructor Vacío (OBLIGATORIO para el DAO)
    public Usuario() {
    }

    // Constructor Lleno
    public Usuario(int id, String username, String password, Rol rol) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    // SECCIÓN DE GETTERS Y SETTERS

    // ID
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // USERNAME
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // PASSWORD
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // ROL
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}