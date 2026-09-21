package com.universidad.confudes.certificados;

import org.springframework.stereotype.Service;

@Service
public class ServicioEmisionCertificados {

    private static final double PORCENTAJE_ASISTENCIA_MINIMA = 0.8;
    private static final String PLANTILLA = "plantilla-2026";
    private static final String CERTIFICADO_INSTITUCIONAL = "cert-udes-2026.pfx";

    private final ValidadorAsistencia validador;
    private final GeneradorCertificadoPDF generador;
    private final FirmaDigitalService firma;
    private final EnvioCorreoService correo;

    public ServicioEmisionCertificados(ValidadorAsistencia validador, GeneradorCertificadoPDF generador,
                                        FirmaDigitalService firma, EnvioCorreoService correo) {
        this.validador = validador;
        this.generador = generador;
        this.firma = firma;
        this.correo = correo;
    }

    public String emitir(String eventoId, String participanteId, String nombre, String correoDestino) {
        if (!validador.tieneAsistenciaMinima(participanteId, eventoId, PORCENTAJE_ASISTENCIA_MINIMA)) {
            throw new AsistenciaInsuficienteException("Asistencia insuficiente");
        }

        byte[] doc = generador.iniciarDocumento(PLANTILLA);
        generador.insertarDatosParticipante(doc, nombre, eventoId, "2026-08-06");
        byte[] documentoFinal = generador.finalizarDocumento();

        FirmaDigitalService.Sesion sesion = firma.abrirSesion(CERTIFICADO_INSTITUCIONAL);
        byte[] documentoFirmado = firma.firmar(sesion, documentoFinal);
        firma.cerrarSesion(sesion);

        correo.adjuntarArchivo(correoDestino, documentoFirmado, "certificado-" + participanteId + ".pdf");
        correo.enviar("Su certificado de participación", "Adjunto encontrará su certificado.");

        return "Certificado emitido y enviado";
    }
}