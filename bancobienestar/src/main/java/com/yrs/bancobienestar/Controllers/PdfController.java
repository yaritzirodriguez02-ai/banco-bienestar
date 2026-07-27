package com.yrs.bancobienestar.Controllers;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yrs.bancobienestar.Modelo.MovimientosEntity;
import com.yrs.bancobienestar.Modelo.SolicitudCreditoEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.MovimientoCuentaRepository;
import com.yrs.bancobienestar.Repository.SolicitudCreditoRepository;
import com.yrs.bancobienestar.Repository.UsuarioRepository;
import com.yrs.bancobienestar.Service.PdfService;

@Controller
@RequestMapping("/pdf")
public class PdfController {

    private final PdfService pdfService;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCuentaRepository movimientoRepository;
    private final SolicitudCreditoRepository solicitudCreditoRepository;

    public PdfController(PdfService pdfService, UsuarioRepository usuarioRepository,
                         MovimientoCuentaRepository movimientoRepository,
                         SolicitudCreditoRepository solicitudCreditoRepository) {
        this.pdfService = pdfService;
        this.usuarioRepository = usuarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.solicitudCreditoRepository = solicitudCreditoRepository;
    }

    // PDF 1: Estado de cuenta del Cliente
    @GetMapping("/historial-cliente")
    public ResponseEntity<InputStreamResource> descargarHistorialCliente(Authentication auth) {
        String username = auth.getName();
        UsuarioEntity cliente = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<MovimientosEntity> movimientos = movimientoRepository.findAll(); // O filtrar por la cuenta del cliente

        ByteArrayInputStream pdf = pdfService.generarEstadoCuentaPdf(cliente, movimientos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=Estado_De_Cuenta.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }

    // PDF 2: Voucher de Solicitud de Crédito
    @GetMapping("/voucher-credito/{id}")
    public ResponseEntity<InputStreamResource> descargarVoucherCredito(@PathVariable Long id) {
        SolicitudCreditoEntity credito = solicitudCreditoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        ByteArrayInputStream pdf = pdfService.generarVoucherCreditoPdf(credito);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=Voucher_Credito_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }

    // PDF 3: Reporte Ejecutivo
    @GetMapping("/reporte-ejecutivo")
    public ResponseEntity<InputStreamResource> descargarReporteEjecutivo() {
        List<UsuarioEntity> clientes = usuarioRepository.findAll().stream()
                .filter(u -> "CLIENTE".equals(u.getRol()))
                .collect(Collectors.toList());

        ByteArrayInputStream pdf = pdfService.generarReporteEjecutivoPdf(clientes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=Reporte_Ejecutivo_Cartera.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}