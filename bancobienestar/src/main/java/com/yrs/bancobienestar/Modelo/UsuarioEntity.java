package com.yrs.bancobienestar.Modelo;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol;

    // CAMPOS NUEVOS PARA DATOS PERSONALES
    @Column(nullable = true)
    private String telefono;

    @Column(nullable = true)
    private String domicilio;

    // Relación con muchas cuentas
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CuentaEntity> cuentas;

    // GETTERS Y SETTERS EXISTENTES

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public List<CuentaEntity> getCuentas() {
        return cuentas;
    }

    public void setCuentas(List<CuentaEntity> cuentas) {
        this.cuentas = cuentas;
    }

    // GETTERS Y SETTERS AGREGADOS (Teléfono y Domicilio)

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }
}