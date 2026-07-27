package com.yrs.bancobienestar.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }// metodo para necryptar la contraseña

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                // Recursos Publicos
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                // rutas permitidas para clientes y ejecutivos
                // SE AGREGÓ: "/procesar-deposito" para habilitar la nueva acción del formulario
                .requestMatchers("/dashboard", "/tranferencias", "/procesar-transferencia", "/procesar-deposito", "/credito",
                        "/procesar-credito", "/api/v1/finanzas/**")
                .authenticated()
                
                // SE AGREGARON: Reglas explícitas por Rol para las vistas de perfil
                .requestMatchers("/perfil/cliente").hasRole("CLIENTE")
                .requestMatchers("/perfil/ejecutivo").hasRole("EJECUTIVO")

                // Panel del admin por rol del ejecutivo
                .requestMatchers("/admin/**").hasRole("EJECUTIVO")
                .anyRequest().authenticated())

                .formLogin(form -> form
                        // Especificaciamos la vista del login personalizado
                        .loginPage("/login")
                        // el formulario envia el campo "userName", no el "username" por defecto
                        .usernameParameter("userName")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()

                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());
        return http.build();

    }

}