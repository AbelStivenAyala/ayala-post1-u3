package com.universidad.confudes.acceso;

public class AccesoDenegadoException extends SecurityException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}