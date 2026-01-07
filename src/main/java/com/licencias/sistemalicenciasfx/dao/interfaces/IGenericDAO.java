package com.licencias.sistemalicenciasfx.dao.interfaces;

import java.util.List;

/**
 * Interfaz Genérica que define las operaciones CRUD estándar
 * @param <T> El tipo de Entidad (ej: Usuario, Tramite, Licencia)
 */
public interface IGenericDAO<T> {

    // Guardar un nuevo registro en la base de datos
    void create(T entity);

    // Actualizar un registro existente
    void update(T entity);

    // Eliminar un registro por su ID
    void delete(int id);

    // Buscar un registro por su ID
    T findById(int id);

    // Obtener todos los registros de la tabla
    List<T> findAll();
}