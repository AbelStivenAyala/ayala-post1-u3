package com.universidad.confudes.certificados;

/**
 * DECORATOR (clase base abstracta).
 *
 * Cada mejora concreta implementa ServicioCertificados y envuelve otro
 * ServicioCertificados (la base, u otro decorador ya aplicado). Siempre
 * delega en el envuelto y transforma el resultado — nunca decide si delega
 * o no. Esa es justamente la diferencia con la Necesidad 4 (ver README):
 * un decorador SIEMPRE termina llamando al objeto real.
 */
public abstract class MejoraCertificadoDecorator implements ServicioCertificados {

    protected final ServicioCertificados envuelto;

    protected MejoraCertificadoDecorator(ServicioCertificados envuelto) {
        this.envuelto = envuelto;
    }
}