package com.example.cascadas;

import java.util.ArrayList;

public class ProductoOrdenado {
    private String producto;

    private String tiempo;
    private double precio;
    private String area;

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    private int cantidad;
    private String descripcion;

    public ProductoOrdenado(String producto, double precio, String area, int cantidad, String descripcion) {
        this.producto = producto;
        this.precio = precio;
        this.area = area;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
    }

    public void setTiempo(String tiempo){
        this.tiempo=tiempo;
    }

    public String getTiempo(){
        return tiempo;
    }


    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }



    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
