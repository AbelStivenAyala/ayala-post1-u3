package com.universidad.confudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.universidad.confudes.certificados.EnvioCorreoService;
import com.universidad.confudes.certificados.FirmaDigitalService;
import com.universidad.confudes.certificados.GeneradorCertificadoPDF;
import com.universidad.confudes.certificados.ValidadorAsistencia;
import com.universidad.confudes.externo.qrcheck.QRCheckClient;

@SpringBootApplication
public class ConfUdesApp {
    public static void main(String[] args) {
        SpringApplication.run(ConfUdesApp.class, args);
    }

    @Bean
    public QRCheckClient qrCheckClient() {
        return new QRCheckClient();
    }

    @Bean
    public ValidadorAsistencia validadorAsistencia() {
        return new ValidadorAsistencia();
    }

    @Bean
    public GeneradorCertificadoPDF generadorCertificadoPDF() {
        return new GeneradorCertificadoPDF();
    }

    @Bean
    public FirmaDigitalService firmaDigitalService() {
        return new FirmaDigitalService();
    }

    @Bean
    public EnvioCorreoService envioCorreoService() {
        return new EnvioCorreoService();
    }
}