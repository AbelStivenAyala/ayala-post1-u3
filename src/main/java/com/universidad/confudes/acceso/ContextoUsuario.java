package com.universidad.confudes.acceso;


public class ContextoUsuario {
    public static String rolActual() {
        return System.getProperty("confudes.rol", "PARTICIPANTE");
    }
}