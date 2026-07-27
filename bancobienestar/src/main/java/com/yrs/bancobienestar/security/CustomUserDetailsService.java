package com.yrs.bancobienestar.security;



import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.UsuarioRepository;

@Service

public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuario) {
        this.usuarioRepository = usuario;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UsuarioEntity usuarioLogin = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontro el cliente"));

        // aqui se usa el builde del spring securrity
        // se agrega automaticamente el rol del usuario
        return User.builder()
                .username(usuarioLogin.getUserName())
                .password(usuarioLogin.getPassword())
                .roles(usuarioLogin.getRol())
                .build();

    }
}

