package com.yrs.bancobienestar.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yrs.bancobienestar.Modelo.CuentaEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {
    Optional<CuentaEntity> findByClabe(String clabe);

    List<CuentaEntity> findByUsuario(UsuarioEntity usuario);
}
