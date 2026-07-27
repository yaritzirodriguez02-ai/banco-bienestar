package com.yrs.bancobienestar.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.yrs.bancobienestar.Modelo.MovimientosEntity;

@Repository
public interface MovimientoCuentaRepository extends JpaRepository<MovimientosEntity, Long> {
    
    //  métodos originales
    List<MovimientosEntity> findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(String cuentaOrigen, String cuentaDestino);
    List<MovimientosEntity> findByCuentaOrigen(String cuentaOrigen);

    // NUEVO MÉTODO (Solo se agrega al final para la gráfica del usuario logueado)
    @Query("SELECT m FROM MovimientosEntity m WHERE m.cuentaOrigen = :clabe AND MONTH(m.fecha) = MONTH(CURRENT_DATE) AND YEAR(m.fecha) = YEAR(CURRENT_DATE)")
    List<MovimientosEntity> findGastosDelMesByClabe(@Param("clabe") String clabe);
}