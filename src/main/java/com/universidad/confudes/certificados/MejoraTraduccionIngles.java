package com.universidad.confudes.certificados;

public class MejoraTraduccionIngles extends MejoraCertificadoDecorator {

    public MejoraTraduccionIngles(ServicioCertificados envuelto) {
        super(envuelto);
    }

    @Override
    public byte[] emitir(SolicitudCertificado solicitud) {
        byte[] documento = envuelto.emitir(solicitud);
        return UtilidadesPDF.traducirAIngles(documento);
    }
}