package com.universidad.confudes.certificados;

public class MejoraMarcaDeAgua extends MejoraCertificadoDecorator {

    private static final String TEXTO_MARCA = "ConfUDES - Documento Oficial";

    public MejoraMarcaDeAgua(ServicioCertificados envuelto) {
        super(envuelto);
    }

    @Override
    public byte[] emitir(SolicitudCertificado solicitud) {
        byte[] documento = envuelto.emitir(solicitud);
        return UtilidadesPDF.aplicarMarcaDeAgua(documento, TEXTO_MARCA);
    }
}