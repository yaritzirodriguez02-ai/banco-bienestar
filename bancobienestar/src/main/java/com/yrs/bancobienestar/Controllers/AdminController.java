package com.yrs.bancobienestar.Controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yrs.bancobienestar.Modelo.CuentaEntity;
import com.yrs.bancobienestar.Modelo.SolicitudCreditoEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.SolicitudCreditoRepository;
import com.yrs.bancobienestar.Repository.UsuarioRepository;
import com.yrs.bancobienestar.Service.BancaService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BancaService bancaService;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudCreditoRepository solicitudCreditoRepository;

    public AdminController(BancaService bancaService, UsuarioRepository usuarioRepository,
            SolicitudCreditoRepository solicitudCreditoRepository) {
        this.bancaService = bancaService;
        this.usuarioRepository = usuarioRepository;
        this.solicitudCreditoRepository = solicitudCreditoRepository;
    }

    private void cargarDatos(Model modelo) {
        List<UsuarioEntity> clientes = usuarioRepository.findAll().stream()
                .filter(u -> "CLIENTE".equals(u.getRol()))
                .collect(Collectors.toList());

        List<SolicitudCreditoEntity> solicitudes = solicitudCreditoRepository.findAllByOrderByFechaDesc();

        modelo.addAttribute("clientes", clientes);
        modelo.addAttribute("solicitudes", solicitudes);
    }

    // 1. Panel Ejecutivo completo (Muestra todo)
    @GetMapping("/dashboard")
    public String mostrarDashboard(Model modelo) {
        cargarDatos(modelo);
        modelo.addAttribute("seccion", "todo");
        return "admin";
    }

    // 2. Opción "Clientes Registrados" (Muestra solo Clientes)
    @GetMapping("/clientes")
    public String mostrarClientes(Model modelo) {
        cargarDatos(modelo);
        modelo.addAttribute("seccion", "clientes");
        return "admin";
    }

    // 3. Opción "Solicitudes de Créditos" (Muestra solo Créditos)
    @GetMapping("/creditos")
    public String mostrarCreditos(Model modelo) {
        cargarDatos(modelo);
        modelo.addAttribute("seccion", "creditos");
        return "admin";
    }

    // WEA PARA CLIENTES
    @PostMapping("/crear-cliente")
    public String crearCliente(@RequestParam String nombre, @RequestParam String username,
            @RequestParam String password,
            @RequestParam Double saldoInicial,
            RedirectAttributes redirectAttributes) {

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() ||
                nombre == null || nombre.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Debes llenar todos los campos");
            return "redirect:/admin/dashboard";
        }

        if (saldoInicial == null || saldoInicial <= 0) {
            redirectAttributes.addAttribute("error", "El monto de apertura debe ser mayor a 0");
            return "redirect:/admin/dashboard";
        }

        try {
            bancaService.crearClienteConCuenta(nombre, username, password, saldoInicial);
            redirectAttributes.addAttribute("exito", "Cliente creado exitosamente");
            return "redirect:/admin/clientes";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // Aprobación de crédito por parte del ejecutivo
    @PostMapping("/aprobar-credito/{id}")
    public String aprobarCredito(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bancaService.aprobarSolicitudCredito(id);
            redirectAttributes.addAttribute("exito", "Crédito aprobado y abonado correctamente");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/creditos";
    }

    // Rechazo de crédito por parte del ejecutivo
    @PostMapping("/rechazar-credito/{id}")
    public String rechazarCredito(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bancaService.rechazarSolicitudCredito(id);
            redirectAttributes.addAttribute("exito", "Solicitud de crédito rechazada");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/creditos";
    }

    // ENDPOINTS AGREGADOS Y COMPLETADOS: CRUD CLIENTES
    // EDITAR DATOS DEL CLIENTE (Nombre, Usuario, Saldo, CLABE)
    @PostMapping("/clientes/editar")
    public String editarCliente(@RequestParam Long id,
                                @RequestParam String nombre,
                                @RequestParam String username,
                                @RequestParam(required = false) String clabe,
                                @RequestParam(required = false) Double saldo,
                                RedirectAttributes redirectAttributes) {
        try {
            UsuarioEntity cliente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            cliente.setNombre(nombre);
            cliente.setUserName(username);

            // CORRECCIÓN AQUÍ: Obtenemos el elemento 0 de la lista getCuentas()
            if (cliente.getCuentas() != null && !cliente.getCuentas().isEmpty()) {
                CuentaEntity cuenta = cliente.getCuentas().get(0);

                if (clabe != null && !clabe.trim().isEmpty()) {
                    cuenta.setClabe(clabe);
                }
                if (saldo != null) {
                    cuenta.setSaldo(saldo);
                }
            }

            usuarioRepository.save(cliente);
            redirectAttributes.addAttribute("exito", "Cliente " + username + " actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error al actualizar cliente: " + e.getMessage());
        }
        return "redirect:/admin/clientes";
    }

    // Cambiar estado de la cuenta del cliente (ACTIVA / SUSPENDIDA)
    @PostMapping("/clientes/actualizar-estado")
    public String actualizarEstadoCliente(@RequestParam Long id, 
                                          @RequestParam String estado, 
                                          RedirectAttributes redirectAttributes) {
        try {
            bancaService.actualizarEstadoCuentaCliente(id, estado);
            redirectAttributes.addAttribute("exito", "Estado del cliente actualizado a " + estado);
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error al actualizar estado: " + e.getMessage());
        }
        return "redirect:/admin/clientes";
    }

    // Eliminar un cliente de la base de datos
    @PostMapping("/clientes/eliminar")
    public String eliminarCliente(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            bancaService.eliminarCliente(id);
            redirectAttributes.addAttribute("exito", "Cliente eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error al eliminar cliente: " + e.getMessage());
        }
        return "redirect:/admin/clientes";
    }

}