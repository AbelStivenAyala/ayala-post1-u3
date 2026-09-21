package com.universidad.confudes.acceso;

import com.universidad.confudes.certificados.ServicioCertificados;
import com.universidad.confudes.certificados.SolicitudCertificado;

/**
 * PROXY (de protección).
 *
 * Controla el acceso a un ServicioCertificados real verificando el rol del
 * usuario ANTES de delegar. A diferencia de un Decorator (Necesidad 3), este
 * objeto puede negar el acceso por completo, sin que el objeto real llegue
 * a ejecutarse — evitando así la operación costosa cuando el usuario no
 * tiene permiso.
 *
 * El resto del sistema sigue inyectando ServicioCertificados sin saber que
 * existe esta verificación: este proxy es intercambiable con cualquier otra
 * implementación de la interfaz (la base, o una cadena de decoradores).
 */
public class ServicioCertificadosProxyControlAcceso implements ServicioCertificados {

    private static final String ROL_ORGANIZADOR = "ORGANIZADOR";
    private static final String ROL_ADMIN = "ADMIN";

    private final ServicioCertificados servicioReal;

    public ServicioCertificadosProxyControlAcceso(ServicioCertificados servicioReal) {
        this.servicioReal = servicioReal;
    }

    @Override
    public byte[] emitir(SolicitudCertificado solicitud) {
        String rol = ContextoUsuario.rolActual();
        if (!ROL_ORGANIZADOR.equals(rol) && !ROL_ADMIN.equals(rol)) {
            throw new AccesoDenegadoException(
                "Rol '" + rol + "' no autorizado para la descarga masiva de certificados");
        }
        return servicioReal.emitir(solicitud);
    }
}
