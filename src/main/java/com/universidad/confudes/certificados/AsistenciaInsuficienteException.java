package com.universidad.confudes.certificados;

public class AsistenciaInsuficienteException extends RuntimeException {
    public AsistenciaInsuficienteException(String mensaje) {
        super(mensaje);
    }
}