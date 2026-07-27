package com.yrs.bancobienestar.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yrs.bancobienestar.Modelo.MovimientosEntity;
import com.yrs.bancobienestar.Repository.MovimientoCuentaRepository;
import com.yrs.bancobienestar.Service.BancaService;

@Controller
@RequestMapping("/admin/movimientos")
public class MovimientoAdminController {

    private final BancaService bancaService;
    private final MovimientoCuentaRepository movimientoCuentaRepository;

    // Inyectamos BancaService y MovimientoCuentaRepository
    public MovimientoAdminController(BancaService bancaService, MovimientoCuentaRepository movimientoCuentaRepository) {
        this.bancaService = bancaService;
        this.movimientoCuentaRepository = movimientoCuentaRepository;
    }

    // Mostramos la vista con la lista de movimientos
    @GetMapping
    public String listaMovimientos(Model model) {
        model.addAttribute("movimientos", bancaService.todosMovimientos());
        return "adminmovimientos";
    }

    // Acción para actualizar el estado (AUTORIZADO / CANCELADO)
    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam Long id, 
                                   @RequestParam String estado, 
                                   RedirectAttributes redirectAttributes) {
        try {
            bancaService.actualizarMovimientos(id, estado);
            redirectAttributes.addAttribute("exito", "El estado de movimiento cambio a ." + estado);
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "No se puede actualizar.");
        }
        return "redirect:/admin/movimientos";
    }

    // =======================================================
    // MÉTODO AGREGADO: EDITAR MOVIMIENTO (Resuelve el error 404)
    // =======================================================
    @PostMapping("/editar")
    public String editarMovimiento(@RequestParam Long id,
                                   @RequestParam Double monto,
                                   @RequestParam String descripcion,
                                   @RequestParam String tipo,
                                   RedirectAttributes redirectAttributes) {
        try {
            MovimientosEntity mov = movimientoCuentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
                
            mov.setMonto(monto);
            mov.setDescripcion(descripcion);
            mov.setTipo(tipo);
            
            movimientoCuentaRepository.save(mov);
            redirectAttributes.addAttribute("exito", "Movimiento #" + id + " actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error al editar movimiento: " + e.getMessage());
        }
        return "redirect:/admin/movimientos";
    }

    // eliminar movimiento
    @PostMapping("/eliminar")
    public String eliminarMovimiento(@RequestParam Long id,
        RedirectAttributes redirectAttributes){
         try{
            bancaService.eliminarMovimiento(id);
            redirectAttributes.addAttribute("exito","Movimiento Eliminado");
         } catch(Exception e){
            redirectAttributes.addAttribute("error", "El error es : " + e);

         }
       return "redirect:/admin/movimientos";
    }
    
}