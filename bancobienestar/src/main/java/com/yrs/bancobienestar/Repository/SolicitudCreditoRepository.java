package com.yrs.bancobienestar.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yrs.bancobienestar.Modelo.SolicitudCreditoEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;

@Repository
public interface SolicitudCreditoRepository extends JpaRepository<SolicitudCreditoEntity, Long> {
    List<SolicitudCreditoEntity> findByUsuarioOrderByFechaDesc(UsuarioEntity usuario);

    List<SolicitudCreditoEntity> findAllByOrderByFechaDesc();
}
