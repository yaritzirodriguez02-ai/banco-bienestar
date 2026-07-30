package com.yrs.bancobienestar.Controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yrs.bancobienestar.Modelo.CuentaEntity;
import com.yrs.bancobienestar.Modelo.SolicitudCreditoEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.SolicitudCreditoRepository;
import com.yrs.bancobienestar.Repository.UsuarioRepository;
import com.yrs.bancobienestar.Service.BancaService;

@Controller
public class CreditoController {

    private final BancaService bancaService;
    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final UsuarioRepository usuarioRepository;

    public CreditoController(BancaService bancaService, SolicitudCreditoRepository solicitudCreditoRepository,
            UsuarioRepository usuarioRepository) {
        this.bancaService = bancaService;
        this.solicitudCreditoRepository = solicitudCreditoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/credito")
    public String mostrarFormularioCredito(Model modelo, Authentication auth) {
        String username = auth.getName();
        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        List<SolicitudCreditoEntity> solicitudes = solicitudCreditoRepository
                .findByUsuarioOrderByFechaDesc(usuario);

        modelo.addAttribute("solicitudes", solicitudes);

        return "credito";
    }

    @PostMapping("/procesar-credito")
    public String procesarCredito(
        @RequestParam Double monto,
        @RequestParam String firmaBase64,
        Authentication auth,
        RedirectAttributes redirectAttributes) {

        String username = auth.getName();

        if (monto == null || monto <= 0) {
            redirectAttributes.addFlashAttribute("error", "El monto debe ser mayor a 0");
            return "redirect:/credito";
        }

        if (firmaBase64 == null || firmaBase64.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La firma es obligatoria");
            return "redirect:/credito";
        }

        try {
            bancaService.guardarSolicitudCredito(username, monto, firmaBase64);
            redirectAttributes.addFlashAttribute("exito", "Crédito firmado y en espera de validación");
            return "redirect:/credito";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/credito";
        }
    }

    // ENDPOINT PARA ABONAR AL CRÉDITO APROBADO (MEJORADO)
    @PostMapping("/credito/abonar")
    public String abonarCredito(@RequestParam Long creditoId,
                                @RequestParam Double monto,
                                @RequestParam(defaultValue = "/credito") String redirect,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            bancaService.abonarACredito(creditoId, monto, authentication.getName());
            
            // Obtener el saldo actualizado para mostrarlo
            UsuarioEntity usuario = usuarioRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            String saldoStr = "";
            if (usuario.getCuentas() != null && !usuario.getCuentas().isEmpty()) {
                Double nuevoSaldo = usuario.getCuentas().get(0).getSaldo();
                saldoStr = " | Saldo actual: $" + String.format("%.2f", nuevoSaldo);
            }
            
            redirectAttributes.addFlashAttribute("exito", "Abono de $" + String.format("%.2f", monto) + " realizado." + saldoStr);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + redirect;
    }
}