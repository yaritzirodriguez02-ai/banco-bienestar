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

    // NUEVO ENDPOINT: Flujo mensual para la gráfica de barras (Ingresos vs Gastos)
    @GetMapping("/flujo-mensual")
    public Map<String, Object> obtenerFlujoMensual(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Usuario no identificado");
        }

        String username = auth.getName();
        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            throw new RuntimeException("El usuario no tiene cuenta");
        }

        String clabe = usuario.getCuentas().get(0).getClabe();

        // Usar solo movimientos del mes actual (consistente con findGastosDelMesByClabe)
        List<MovimientosEntity> movimientosOrigen = movimientoCuentaRepository.findGastosDelMesByClabe(clabe);
        
        // También obtener ingresos del mes actual (depósitos a la cuenta del cliente)
        // Reutilizamos el repositorio con una consulta similar pero para cuenta_destino
        List<MovimientosEntity> movimientosDestino = movimientoCuentaRepository.findIngresosDelMesByClabe(clabe);

        // Inicializar datos por semanas (4 semanas)
        double[] ingresos = new double[4];
        double[] gastos = new double[4];

        java.time.LocalDate hoy = java.time.LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        // Procesar gastos (cuentaOrigen = clabe)
        for (MovimientosEntity m : movimientosOrigen) {
            if (m.getFecha() == null) continue;
            // Solo mes actual
            if (m.getFecha().getMonthValue() != mesActual || m.getFecha().getYear() != anioActual) continue;

            int day = m.getFecha().getDayOfMonth();
            int semana = (day - 1) / 7;
            if (semana > 3) semana = 3;

            double monto = m.getMonto() != null ? m.getMonto() : 0.0;
            gastos[semana] += monto;
        }

        // Procesar ingresos (cuentaDestino = clabe, excluyendo auto-referencias)
        for (MovimientosEntity m : movimientosDestino) {
            if (m.getFecha() == null) continue;
            // Solo mes actual
            if (m.getFecha().getMonthValue() != mesActual || m.getFecha().getYear() != anioActual) continue;

            int day = m.getFecha().getDayOfMonth();
            int semana = (day - 1) / 7;
            if (semana > 3) semana = 3;

            double monto = m.getMonto() != null ? m.getMonto() : 0.0;
            ingresos[semana] += monto;
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("etiquetas", Arrays.asList("Semana 1", "Semana 2", "Semana 3", "Semana 4"));
        resultado.put("ingresos", Arrays.asList(ingresos[0], ingresos[1], ingresos[2], ingresos[3]));
        resultado.put("gastos", Arrays.asList(gastos[0], gastos[1], gastos[2], gastos[3]));

        return resultado;
    }

}