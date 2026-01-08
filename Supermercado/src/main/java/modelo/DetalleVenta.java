/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Toledo
 */

public class DetalleVenta {
    private int idDetalle;
    private int idVenta;
    private int idProducto;
    private Producto producto; // Referencia al producto
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    
    public DetalleVenta() {}
    
    public DetalleVenta(int idVenta, int idProducto, int cantidad, double precioUnitario, double subtotal) {
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }
    
    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.idProducto = producto.getIdProducto();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta();
        calcularSubtotal();
    }
    
    public void calcularSubtotal() {
        if (producto != null) {
            this.subtotal = this.cantidad * this.precioUnitario;
        } else {
            this.subtotal = this.cantidad * this.precioUnitario;
        }
    }
    
    // Getters y Setters
    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }
    
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { 
        this.producto = producto; 
        this.idProducto = producto.getIdProducto();
        this.precioUnitario = producto.getPrecioVenta();
    }
    
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad; 
        calcularSubtotal();
    }
    
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { 
        this.precioUnitario = precioUnitario; 
        calcularSubtotal();
    }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    @Override
    public String toString() {
        if (producto != null) {
            return producto.getNombre() + " x" + cantidad + " = S/" + subtotal;
        }
        return "Producto ID: " + idProducto + " x" + cantidad + " = S/" + subtotal;
    }
}