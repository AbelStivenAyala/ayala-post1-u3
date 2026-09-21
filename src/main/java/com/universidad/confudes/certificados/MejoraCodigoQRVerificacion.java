package com.universidad.confudes.certificados;

public class MejoraCodigoQRVerificacion extends MejoraCertificadoDecorator {

    private static final String BASE_URL_VERIFICACION = "https://confudes.edu.co/verificar/";

    public MejoraCodigoQRVerificacion(ServicioCertificados envuelto) {
        super(envuelto);
    }

    @Override
    public byte[] emitir(SolicitudCertificado solicitud) {
        byte[] documento = envuelto.emitir(solicitud);
        String urlVerificacion = BASE_URL_VERIFICACION + solicitud.getParticipanteId();
        return UtilidadesPDF.insertarCodigoQR(documento, urlVerificacion);
    }
}