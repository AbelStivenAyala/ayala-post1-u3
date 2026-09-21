package com.universidad.confudes.certificados;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Depende únicamente de ServicioCertificados (el Facade, o cualquier
// decorador/proxy que lo implemente — ver Necesidades 3 y 4).
@RestController
@RequestMapping("/api/certificados")
public class ControladorCertificados {
    private final ServicioCertificados servicioCertificados;

    public ControladorCertificados(ServicioCertificados servicioCertificados) {
        this.servicioCertificados = servicioCertificados;
    }

    @PostMapping("/{eventoId}/{participanteId}")
    public ResponseEntity<String> emitir(@PathVariable String eventoId, @PathVariable String participanteId,
                                          @RequestParam String nombre, @RequestParam String correoDestino) {
        try {
            SolicitudCertificado solicitud = new SolicitudCertificado(eventoId, participanteId, nombre, correoDestino);
            servicioCertificados.emitir(solicitud);
            return ResponseEntity.ok("Certificado emitido y enviado");
        } catch (AsistenciaInsuficienteException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}