package com.yrs.bancobienestar.Config;

import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;




//LA UNICA FUNCION ES CREAR UN USUSARIO EJECUTIVO Y UN USUSARIO CLIENTE
@Component

public class DataInitializer implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuario, PasswordEncoder pass) {
        this.usuarioRepository = usuario;
        this.passwordEncoder = pass;
    }

    @Override
    public void run(String... args) throws Exception {

        if (usuarioRepository.count() == 0) {
            // Crear un usuario ejecutivo
            System.out.println("Agregando Prueba usuario Ejecutivo...");
            // 1 Creando un usuario ejecutivo
            UsuarioEntity ejecutivo = new UsuarioEntity();
            ejecutivo.setUserName("YRS");
            ejecutivo.setNombre("Yaritzi Rodriguez Sanchez");
            ejecutivo.setPassword(passwordEncoder.encode("yari1234"));
            ejecutivo.setRol("EJECUTIVO");
            usuarioRepository.save(ejecutivo);
            System.out.println("Usuario Ejecutivo agregado: YRS-yari1234");

            // 2 Creando un usuario cliente
            UsuarioEntity cliente = new UsuarioEntity();
            cliente.setUserName("acapulco");
            cliente.setNombre("Brandon Acapulco Bedolla");
            cliente.setPassword(passwordEncoder.encode("acapulco1234"));
            cliente.setRol("CLIENTE");
            usuarioRepository.save(cliente);
            System.out.println("Usuario Cliente agregado: acapulco-acapulco1234");

        }

    }
}