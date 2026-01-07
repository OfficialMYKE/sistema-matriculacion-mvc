package com.licencias.sistemalicenciasfx.dao.interfaces;

import com.licencias.sistemalicenciasfx.model.entities.Usuario;

/**
 * Interfaz para el DAO de Usuario.
 * Define las operaciones específicas para Usuarios (como login)
 * Extiende de IGenericDAO para tener ya listos los métodos CRUD (create, update, delete, findAll).
 */
public interface IUsuarioDAO extends IGenericDAO<Usuario> {

    // Método único y específico para usuarios
    Usuario login(String username, String password);

}