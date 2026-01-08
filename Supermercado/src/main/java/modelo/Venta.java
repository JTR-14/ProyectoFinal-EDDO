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

public class Venta {
    private int idVenta;
    private String fecha;
    private double montoTotal;
    private int idUsuario;
    private int idCliente;
    private String nombreCliente;
    private String nombreUsuario;
    private ArrayList<DetalleVenta> detalles;
    
    public Venta() {
        this.detalles = new ArrayList<>();
        // Fecha actual por defecto
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
        this.detalles.add(detalle);
        calcularMontoTotal();
    }
    
    // Método para eliminar detalle
    public void eliminarDetalle(int index) {
        if (index >= 0 && index < detalles.size()) {
            detalles.remove(index);
            calcularMontoTotal();
        }
    }
    
    // Método para calcular el monto total
    public void calcularMontoTotal() {
        montoTotal = 0;
        for (DetalleVenta detalle : detalles) {
            montoTotal += detalle.getSubtotal();
        }
    }
    
    // Método para verificar si hay stock disponible
    public boolean verificarStockDisponible() {
        for (DetalleVenta detalle : detalles) {
            if (detalle.getProducto().getStockActual() < detalle.getCantidad()) {
                return false;
            }
        }
        return true;
    }
    
    // Getters y Setters
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    
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
    
    public ArrayList<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(ArrayList<DetalleVenta> detalles) { 
        this.detalles = detalles; 
        calcularMontoTotal();
    }
    
    @Override
    public String toString() {
        return "Venta #" + idVenta + " - " + fecha + " - Total: S/" + montoTotal;
    }
}