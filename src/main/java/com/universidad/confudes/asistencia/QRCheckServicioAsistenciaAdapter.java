package com.universidad.confudes.asistencia;

import org.springframework.stereotype.Service;

import com.universidad.confudes.externo.qrcheck.QRCheckClient;
import com.universidad.confudes.externo.qrcheck.QRCheckRequest;
import com.universidad.confudes.externo.qrcheck.QRCheckResponse;

@Service
public class QRCheckServicioAsistenciaAdapter implements ServicioAsistencia {

    private static final int CODIGO_OK = 200;

    private final QRCheckClient qrCheckClient;

    public QRCheckServicioAsistenciaAdapter(QRCheckClient qrCheckClient) {
        this.qrCheckClient = qrCheckClient;
    }

    @Override
    public ResultadoCheckIn registrarAsistencia(String eventoId, String participanteId, String credencialQR) {
        long idEventoNumerico = parsearIdEvento(eventoId);

        QRCheckRequest request = new QRCheckRequest(credencialQR, idEventoNumerico);
        QRCheckResponse response = qrCheckClient.validar(request);

        boolean exitoso = response.getCodigoRespuesta() == CODIGO_OK;
        return new ResultadoCheckIn(exitoso, response.getDetalle());
    }

    private long parsearIdEvento(String eventoId) {
        // El proveedor exige un long; el contrato interno maneja eventoId como
        // String (p. ej. "EVT-001"). Se extrae la parte numérica; si no hay
        // ninguna, se usa el hashCode como identificador estable.
        String soloDigitos = eventoId.replaceAll("\\D+", "");
        if (soloDigitos.isEmpty()) {
            return Math.abs((long) eventoId.hashCode());
        }
        return Long.parseLong(soloDigitos);
    }
}