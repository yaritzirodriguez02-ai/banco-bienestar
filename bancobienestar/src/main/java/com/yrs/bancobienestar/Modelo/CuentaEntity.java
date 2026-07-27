package com.yrs.bancobienestar.Modelo;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuentas")
public class CuentaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 18)
    private String clabe;
    @Column(nullable = false)
    private Double saldo;
    // relacion inversa a usuarios
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    private String estado = "PENDIENTE";


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClabe() {
        return clabe;
    }

    public void setClabe(String clabe) {
        this.clabe = clabe;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
