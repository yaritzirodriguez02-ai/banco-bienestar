package com.yrs.bancobienestar.Controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yrs.bancobienestar.Modelo.CuentaEntity;
import com.yrs.bancobienestar.Modelo.MovimientosEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.MovimientoCuentaRepository;
import com.yrs.bancobienestar.Repository.UsuarioRepository;
import com.yrs.bancobienestar.Service.BancaService;

@Controller
public class TransferenciaController {

    private final BancaService bancaService;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCuentaRepository movimientoCuentaRepository;

    public TransferenciaController(BancaService bancaService, UsuarioRepository usuarioRepository,
            MovimientoCuentaRepository movimientoCuentaRepository){
        this.bancaService = bancaService;
        this.usuarioRepository = usuarioRepository;
        this.movimientoCuentaRepository = movimientoCuentaRepository;
    }

    @GetMapping("/transferencias")
    public String mostrarFormTransferencia(Model modelo, Authentication authentication){
        String username = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        String clabe = "No asignada";
        Double saldo = 0.0;
        List<MovimientosEntity> ultimosMovimientos = new ArrayList<>();

        if (usuario.getCuentas() != null && !usuario.getCuentas().isEmpty()){
            CuentaEntity cuentaPrincipal = usuario.getCuentas().get(0);
            clabe = cuentaPrincipal.getClabe();
            saldo = cuentaPrincipal.getSaldo();
            ultimosMovimientos = movimientoCuentaRepository.findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(clabe, clabe);
        }

        modelo.addAttribute("cuentaClabe", clabe);
        modelo.addAttribute("saldoTotal", saldo);
        modelo.addAttribute("movimientos", ultimosMovimientos);
        return "transferencia";
    }

    @PostMapping("/procesar-transferencia")
    public String procesar ( 
       @RequestParam String cuentaDestino,
       @RequestParam Double monto,
       @RequestParam String descripcion,
       Authentication auth){

        String usernameAutorizado = auth.getName();
        try {
            bancaService.transferirDesdeUsuario(usernameAutorizado, cuentaDestino, monto, descripcion);
            return "redirect:/dashboard?exito=" + URLEncoder.encode("Transferencia realizada con éxito", StandardCharsets.UTF_8);
        } catch (Exception e) {
            String errorCodificado = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/transferencias?error=" + errorCodificado;
        }
    }

    // =======================================================
    // MÉTODO AGREGADO PARA EL DEPÓSITO / RECARGA A MI CUENTA
    // =======================================================
    @PostMapping("/procesar-deposito")
    public String procesarDeposito(
       @RequestParam Double monto,
       @RequestParam(required = false) String descripcion,
       Authentication auth){

        String usernameAutorizado = auth.getName();
        try {
            bancaService.depositarAUsuario(usernameAutorizado, monto, descripcion);
            return "redirect:/transferencias?exito=" + URLEncoder.encode("Depósito realizado con éxito", StandardCharsets.UTF_8);
        } catch (Exception e) {
            String errorCodificado = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/transferencias?error=" + errorCodificado;
        }
    }
}