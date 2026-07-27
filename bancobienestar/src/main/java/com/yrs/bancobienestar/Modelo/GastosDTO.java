package com.yrs.bancobienestar.Modelo;

public class GastosDTO {
    private String descripcion;
    private Double monto;
    private String colorHexadecimal;

    public GastosDTO() {
    }

    public GastosDTO(String descripcion, Double monto, String colorHexadecimal) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.colorHexadecimal = colorHexadecimal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getColorHexadecimal() {
        return colorHexadecimal;
    }

    public void setColorHexadecimal(String colorHexadecimal) {
        this.colorHexadecimal = colorHexadecimal;
    }
}
