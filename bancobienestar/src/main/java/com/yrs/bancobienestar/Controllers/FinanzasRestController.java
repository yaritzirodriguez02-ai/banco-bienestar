package com.yrs.bancobienestar.Controllers;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yrs.bancobienestar.Modelo.GastosDTO;
import com.yrs.bancobienestar.Modelo.MovimientosEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.MovimientoCuentaRepository;
import com.yrs.bancobienestar.Repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/finanzas")
public class FinanzasRestController {

    // estancia de los repositorios
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCuentaRepository movimientoCuentaRepository;

    private static final Map<String, String> COLOR_MAP = new HashMap<>();
    static {
        COLOR_MAP.put("Alimentacion", "#FF6384");
        COLOR_MAP.put("Vivienda", "#36A2EB");
        COLOR_MAP.put("Transporte", "#FFCE56");
        COLOR_MAP.put("Otros", "#4BC0C0");
        COLOR_MAP.put("Servicios", "#9966FF");
        COLOR_MAP.put("Ocio", "#FF9F40");
        COLOR_MAP.put("Comida", "#C9CBCF");
        COLOR_MAP.put("Renta", "#2ECC71");
        COLOR_MAP.put("Nomina", "#E74C3C");
      

    }

    private static final List<String> PALETA_COLORES = Arrays
            .asList("#FF6384", "#36A2EB", "#FFCE56",
                    "#4BC0C0", "#9966FF", "#FF9F40", "#C9CBCF", "#2ECC71", "#E74C3C");

    // private final movimientoCuentaRepository movimientoCuentaRepository;
    // private final usuarioRepository usuarioRepository;

    public FinanzasRestController(UsuarioRepository usuarioRepository,
            MovimientoCuentaRepository movimientoCuentaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.movimientoCuentaRepository = movimientoCuentaRepository;
    }

    @GetMapping("/gastos-mes")
    public List<GastosDTO> obtenerGastos(Authentication auth) {
        // datos de ejemplo, en un caso real se obtendrían de una base de datos

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Usuario no identificado");
        }

        String username = auth.getName();
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByUserName(username);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        UsuarioEntity usuario = usuarioOpt.get();
        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            throw new RuntimeException("El ususario no tiene cuenta");
        }

        String clabe = usuario.getCuentas().get(0).getClabe();
        
        // CORRECCIÓN: En lugar de usar findByCuentaOrigen puro que trae datos de todo el historial histórico,
        // invocamos la nueva consulta para que la gráfica represente de manera fiel los movimientos del mes actual del cliente
        List<MovimientosEntity> movimientos = movimientoCuentaRepository.findGastosDelMesByClabe(clabe);

        if (movimientos == null || movimientos.isEmpty()) {
            // Retornamos un arreglo vacío en lugar de lanzar una excepción para evitar que el script de JavaScript
            // rompa la ejecución del canvas si el cliente aún no registra consumos durante el mes en curso.
            return new ArrayList<>(); 
        }

        // agrupar por descripcion y sumar montos

        Map<String, Double> gastosAgrupados = movimientos.stream()
                .collect(Collectors.groupingBy(MovimientosEntity::getDescripcion,
                        Collectors.summingDouble(MovimientosEntity::getMonto)));

        // mapear los DTO y asignar colores

        List<GastosDTO> resultado = new ArrayList<>();
        int colorIdx = 0;
        for (Map.Entry<String, Double> entry : gastosAgrupados.entrySet()) {

            String descripcion = entry.getKey();
            Double monto = entry.getValue();

            String color = COLOR_MAP.get(descripcion.trim());
            if (color == null) {
                color = PALETA_COLORES.get(
                        colorIdx % PALETA_COLORES.size());
                colorIdx++;
            }
            resultado.add(new GastosDTO(descripcion, monto, color));
        }
        return resultado;

    }

}