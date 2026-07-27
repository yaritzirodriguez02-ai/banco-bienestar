package com.yrs.bancobienestar.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yrs.bancobienestar.Modelo.CuentaEntity;
import com.yrs.bancobienestar.Modelo.MovimientosEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.MovimientoCuentaRepository;
import com.yrs.bancobienestar.Repository.UsuarioRepository;
import com.yrs.bancobienestar.Service.BancaService;

@Controller
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final MovimientoCuentaRepository movimientoCuentaRepository;
    private final BancaService bancaService;

    public DashboardController(UsuarioRepository usuario, 
                               MovimientoCuentaRepository movimientoCuenta,
                               BancaService bancaService) {
        this.usuarioRepository = usuario;
        this.movimientoCuentaRepository = movimientoCuenta;
        this.bancaService = bancaService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";

    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model modelo, Authentication auth) {

        // obtener el usuario autenticado
        if (auth == null) {
            return "redirect:/login";

        }

        String userName = auth.getName();
        UsuarioEntity usuario = usuarioRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if ("EJECUTIVO".equals(usuario.getRol())) {
            return "redirect:/admin/dashboard";

        }

        // cargar datos del cliente
        String clabe = "No asignada";
        Double saldo = 0.0;
        List<MovimientosEntity> ultimosMovimientos = new ArrayList<>();

        if (usuario.getCuentas() != null && !usuario.getCuentas().isEmpty()) {
            CuentaEntity cuentaPrincipal = usuario.getCuentas().get(0);
            clabe = cuentaPrincipal.getClabe();
            saldo = cuentaPrincipal.getSaldo();
            ultimosMovimientos = movimientoCuentaRepository
                    .findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(clabe, clabe);

        }

        // inyectar los datos al modelo de thymeleaf
        modelo.addAttribute("nombreCliente", usuario.getNombre());
        modelo.addAttribute("saldoTotal", saldo);
        modelo.addAttribute("cuentaClabe", clabe);
        modelo.addAttribute("movimientos", ultimosMovimientos);

        return "dashboard";
    }

    // ENDPOINT PARA REGISTRAR COMPRAS / GASTOS PROPIOS (EJ. STARBUCKS)
    @PostMapping("/cliente/registrar-gasto")
    public String registrarGastoPropio(@RequestParam String descripcion,
                                       @RequestParam Double monto,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        try {
            bancaService.registrarGastoCliente(authentication.getName(), descripcion, monto);
            redirectAttributes.addAttribute("exito", "Compra en " + descripcion + " registrada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

}