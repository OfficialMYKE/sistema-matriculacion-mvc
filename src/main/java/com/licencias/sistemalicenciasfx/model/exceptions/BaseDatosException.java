package com.licencias.sistemalicenciasfx.model.exceptions;

public class BaseDatosException extends Exception {
    public BaseDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}