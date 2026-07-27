package com.yrs.bancobienestar.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.UsuarioRepository;

@Controller
public class PerfilController {

    private final UsuarioRepository usuarioRepository;

    public PerfilController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/perfil/ejecutivo")
    public String verPerfilEjecutivo(Model modelo, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario ejecutivo no encontrado"));

        modelo.addAttribute("nombreCompleto", usuario.getNombre());
        modelo.addAttribute("username", usuario.getUserName());
        modelo.addAttribute("telefono", usuario.getTelefono());
        modelo.addAttribute("domicilio", usuario.getDomicilio());

        return "perfil-ejecutivo";
    }

    @PostMapping("/perfil/ejecutivo/actualizar")
    public String actualizarPerfilEjecutivo(@RequestParam String nombre,
                                            @RequestParam String telefono,
                                            @RequestParam String domicilio,
                                            Authentication authentication,
                                            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            UsuarioEntity ejecutivo = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Ejecutivo no encontrado"));

            ejecutivo.setNombre(nombre);
            ejecutivo.setTelefono(telefono);
            ejecutivo.setDomicilio(domicilio);

            usuarioRepository.save(ejecutivo);
            redirectAttributes.addFlashAttribute("exito", "Perfil de ejecutivo actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil: " + e.getMessage());
        }
        return "redirect:/perfil/ejecutivo";
    }

    @GetMapping("/perfil/cliente")
    public String verPerfilCliente(Model modelo, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario cliente no encontrado"));

        String clabe = "No asignada";
        if (usuario.getCuentas() != null && !usuario.getCuentas().isEmpty()) {
            clabe = usuario.getCuentas().get(0).getClabe();
        }

        modelo.addAttribute("nombreCompleto", usuario.getNombre());
        modelo.addAttribute("username", usuario.getUserName());
        modelo.addAttribute("telefono", usuario.getTelefono());
        modelo.addAttribute("domicilio", usuario.getDomicilio());
        modelo.addAttribute("clabe", clabe);

        return "perfil-cliente";
    }

    @PostMapping("/perfil/cliente/actualizar")
    public String actualizarPerfilCliente(@RequestParam String nombre,
                                          @RequestParam String telefono,
                                          @RequestParam String domicilio,
                                          Authentication authentication,
                                          RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            UsuarioEntity cliente = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            cliente.setNombre(nombre);
            cliente.setTelefono(telefono);
            cliente.setDomicilio(domicilio);

            usuarioRepository.save(cliente);
            redirectAttributes.addFlashAttribute("exito", "Datos personales actualizados correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil: " + e.getMessage());
        }
        return "redirect:/perfil/cliente";
    }
}