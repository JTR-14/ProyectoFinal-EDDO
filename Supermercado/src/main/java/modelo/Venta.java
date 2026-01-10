/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Toledo
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import utiles.ListaEnlazadaDoble;
import utiles.NodoEnDoble;

public class Venta {
    private int idVenta;
    private String fecha;
    private double subTotal;
    private double montoTotal;
    private int idUsuario;
    private int idCliente;
    private String nombreCliente;
    private String nombreUsuario;
    private double igv;
    private ListaEnlazadaDoble<DetalleVenta> detalles;
    
  public Venta() {
    this.detalles = new ListaEnlazadaDoble<>();
    this.fecha = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
}
    
    public Venta(int idVenta, String fecha, double montoTotal, int idUsuario, int idCliente) {
        this();
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.montoTotal = montoTotal;
        this.idUsuario = idUsuario;
        this.idCliente = idCliente;
    }
    
    // Método para agregar detalle
public void agregarDetalle(DetalleVenta detalle) {
    detalles.insertaAlFinal(detalle);
    calcularMontoTotal();
}
    
    // Método para eliminar detalle
  public void eliminarDetalle(int index) {
    if (index < 0) return;

    int i = 0;
    NodoEnDoble<DetalleVenta> p = detalles.getPrimero();

    while (p != null) {
        if (i == index) {
            detalles.eliminar(p.getInfo());
            calcularSubtotal();
            return;
        }
        i++;
        p = p.getSgte();
    }
}
    
    // Método para calcular el monto total
  public void calcularSubtotal() {
    subTotal = 0;
    NodoEnDoble<DetalleVenta> p = detalles.getPrimero();

    while (p != null) {
        subTotal += p.getInfo().getSubTotal();
        p = p.getSgte();
    }
}
  public void calcularIgv()
  {
  igv = subTotal * 0.18;
  }
    public void calcularMontoTotal() {
    montoTotal = 0;
    montoTotal= subTotal+igv;
  
}
    // Método para verificar si hay stock disponible
public boolean verificarStockDisponible() {
    NodoEnDoble<DetalleVenta> p = detalles.getPrimero();

    while (p != null) {
        DetalleVenta d = p.getInfo();
        if (d.getProducto().getStockActual() < d.getCantidad()) {
            return false;
        }
        p = p.getSgte();
    }
    return true;
}

    
    // Getters y Setters
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
        public double getIgv() { return igv; }
    public void setIgv(double igv) 
        { this.igv = igv; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    
    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }
    
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    
    public ListaEnlazadaDoble<DetalleVenta> getDetalles() 
    { return detalles; }
    
    public void setDetalles(ListaEnlazadaDoble<DetalleVenta> detalles) { 
        this.detalles = detalles; 
        calcularMontoTotal();
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }
    
    @Override
    public String toString() {
        return "Venta #" + idVenta + " - " + fecha + " - Total: S/" + montoTotal;
    }
}