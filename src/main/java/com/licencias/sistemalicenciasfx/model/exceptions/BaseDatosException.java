package com.licencias.sistemalicenciasfx.model.exceptions;

public class BaseDatosException extends Exception {

    // Constructor para mensajes simples
    public BaseDatosException(String mensaje) {
        super(mensaje);
    }

    // Constructor para mensajes + la causa original (el error de SQL)
    public BaseDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}