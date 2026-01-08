/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USER
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Pedido {
    private int idPedido;
    private Cliente cliente;
    private ArrayList<DetallePedido> detalles;
    private String estado; // "PENDIENTE", "EN_PROCESO", "COMPLETADO", "CANCELADO"
    private LocalDateTime fechaHora;
    private String direccionEntrega;
    private String telefonoContacto;
    private String metodoPago; // "EFECTIVO", "TARJETA", "TRANSFERENCIA"
    private String notas;
    
    public Pedido() {
        this.detalles = new ArrayList<>();
        this.fechaHora = LocalDateTime.now();
        this.estado = "PENDIENTE";
        this.metodoPago = "EFECTIVO";
    }
    
    public Pedido(Cliente cliente, String direccionEntrega, String telefonoContacto) {
        this();
        this.cliente = cliente;
        this.direccionEntrega = direccionEntrega;
        this.telefonoContacto = telefonoContacto;
    }
    
    // Método para agregar productos al pedido
    public void agregarProducto(Producto producto, int cantidad) {
        // Verificar si el producto ya existe en el pedido
        for (DetallePedido detalle : detalles) {
            if (detalle.getProducto().getIdProducto() == producto.getIdProducto()) {
                detalle.setCantidad(detalle.getCantidad() + cantidad);
                detalle.calcularSubtotal();
                return;
            }
        }
        
        // Si no existe, agregar nuevo detalle
        DetallePedido nuevoDetalle = new DetallePedido(producto, cantidad);
        detalles.add(nuevoDetalle);
    }
    
    // Método para calcular el total del pedido
    public double calcularTotal() {
        double total = 0;
        for (DetallePedido detalle : detalles) {
            total += detalle.getSubtotal();
        }
        return total;
    }
    
    // Método para verificar disponibilidad de stock
    public boolean verificarStockDisponible() {
        for (DetallePedido detalle : detalles) {
            Producto producto = detalle.getProducto();
            if (producto.getStockActual() < detalle.getCantidad()) {
                return false;
            }
        }
        return true;
    }
    
    // Método para obtener resumen del pedido
    public String obtenerResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Pedido #").append(idPedido).append("\n");
        resumen.append("Cliente: ").append(cliente.getNombre()).append("\n");
        resumen.append("Estado: ").append(estado).append("\n");
        resumen.append("Total: S/").append(String.format("%.2f", calcularTotal())).append("\n");
        resumen.append("Productos: ").append(detalles.size()).append(" items");
        return resumen.toString();
    }
    
    // Método para obtener detalles formateados
    public String obtenerDetallesFormateados() {
        StringBuilder detalleStr = new StringBuilder();
        detalleStr.append("══════════════════════════════════════\n");
        detalleStr.append("           DETALLE DEL PEDIDO         \n");
        detalleStr.append("══════════════════════════════════════\n");
        detalleStr.append("Pedido #: ").append(idPedido).append("\n");
        detalleStr.append("Fecha: ").append(fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        detalleStr.append("Estado: ").append(estado).append("\n");
        detalleStr.append("Cliente: ").append(cliente.getNombre()).append("\n");
        detalleStr.append("DNI: ").append(cliente.getDni()).append("\n");
        detalleStr.append("Teléfono: ").append(telefonoContacto).append("\n");
        detalleStr.append("Dirección: ").append(direccionEntrega).append("\n");
        detalleStr.append("Método Pago: ").append(metodoPago).append("\n");
        detalleStr.append("Notas: ").append(notas != null ? notas : "Ninguna").append("\n");
        detalleStr.append("──────────────────────────────────────\n");
        detalleStr.append("               PRODUCTOS              \n");
        detalleStr.append("──────────────────────────────────────\n");
        
        for (int i = 0; i < detalles.size(); i++) {
            DetallePedido detalle = detalles.get(i);
            detalleStr.append(String.format("%2d. %-20s x%3d = S/ %7.2f\n", 
                i + 1,
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getSubtotal()));
        }
        
        detalleStr.append("──────────────────────────────────────\n");
        double total = calcularTotal();
        double igv = total * 0.18;
        double totalConIGV = total + igv;
        detalleStr.append(String.format("Subtotal:         S/ %10.2f\n", total));
        detalleStr.append(String.format("IGV (18%%):        S/ %10.2f\n", igv));
        detalleStr.append(String.format("TOTAL A PAGAR:    S/ %10.2f\n", totalConIGV));
        detalleStr.append("══════════════════════════════════════\n");
        
        return detalleStr.toString();
    }
    
    // Getters y Setters
    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    
    public ArrayList<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(ArrayList<DetallePedido> detalles) { this.detalles = detalles; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    
    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
    
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    
    @Override
    public String toString() {
        return String.format("Pedido #%03d - %s - S/%.2f - %s", 
            idPedido, cliente.getNombre(), calcularTotal(), estado);
    }
}
