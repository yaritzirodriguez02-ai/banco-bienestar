package com.yrs.bancobienestar.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import java.util.List;


@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByUserName(String username);

    Optional<UsuarioEntity> findByNombre(String nombre);



}
