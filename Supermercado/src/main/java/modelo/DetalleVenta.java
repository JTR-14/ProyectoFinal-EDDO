/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Toledo
 */

public class DetalleVenta implements Comparable<DetalleVenta> {
    private int idDetalle;
    private int idVenta;
    private int idProducto;
    private Producto producto; // Referencia al producto
    private int cantidad;
    private double precioUnitario;
    private double subTotal;
    
    public DetalleVenta() {}
    
    public DetalleVenta(int idVenta, int idProducto, int cantidad, double precioUnitario, double subtotal) {
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subTotal = subtotal;
    }
    
    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.idProducto = producto.getIdProducto();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta();
        calcularSubTotal();
    }
    
    public void calcularSubTotal() {
        if (producto != null) {
            this.subTotal = this.cantidad * this.precioUnitario;
        } else {
            this.subTotal = this.cantidad * this.precioUnitario;
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
        calcularSubTotal();
    }
    
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { 
        this.precioUnitario = precioUnitario; 
        calcularSubTotal();
    }
    
    public double getSubTotal() { return subTotal; }
    public void setSubtotal(double subtotal) { this.subTotal = subtotal; }
    
    @Override
    public String toString() {
        if (producto != null) {
            return producto.getNombre() + " x" + cantidad + " = S/" + subTotal;
        }
        return "Producto ID: " + idProducto + " x" + cantidad + " = S/" + subTotal;
    }
    
    @Override
public int compareTo(DetalleVenta o) {
    return Integer.compare(this.idProducto, o.idProducto);
}
}