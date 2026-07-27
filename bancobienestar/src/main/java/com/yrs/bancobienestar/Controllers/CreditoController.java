package com.yrs.bancobienestar.Controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Authentication auth) {

        String username = auth.getName();

        if (monto == null || monto <= 0) {
            return "redirect:/credito?error=El monto debe ser mayor a 0";
        }

        if (firmaBase64 == null || firmaBase64.trim().isEmpty()) {
            return "redirect:/credito?error=La firma es obligatoria";
        }

        try {
            bancaService.guardarSolicitudCredito(username, monto, firmaBase64);
            return "redirect:/credito?exito=Credito firmado y en espera de validacion";
        } catch (Exception e) {
            return "redirect:/credito?error=" + e.getMessage();
        }
    }

    // ENDPOINT PARA ABONAR AL CRÉDITO APROBADO
    @PostMapping("/credito/abonar")
    public String abonarCredito(@RequestParam Long creditoId,
                                @RequestParam Double monto,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            bancaService.abonarACredito(creditoId, monto, authentication.getName());
            redirectAttributes.addAttribute("exito", "Abono abonado correctamente a su crédito.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/credito";
    }
}