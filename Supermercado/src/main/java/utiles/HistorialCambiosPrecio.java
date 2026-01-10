/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

/**
 *
 * @author USER
 */
import datos.Conexion;
import modelo.Producto;
import javax.swing.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistorialCambiosPrecio {
    
    // Clase interna para representar un cambio de precio
    public class CambioPrecio {
        private int idCambio;
        private Producto producto;
        private double precioAnterior;
        private double precioNuevo;
        private String fechaHora;
        private String motivo;
        private int idUsuario;
        private String nombreUsuario;
        
        public CambioPrecio(Producto producto, double precioAnterior, double precioNuevo, 
                           String motivo, int idUsuario, String nombreUsuario) {
            this.producto = producto;
            this.precioAnterior = precioAnterior;
            this.precioNuevo = precioNuevo;
            this.fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.motivo = motivo;
            this.idUsuario = idUsuario;
            this.nombreUsuario = nombreUsuario;
        }
        
        // Constructor para cargar desde base de datos
        public CambioPrecio(int idCambio, Producto producto, double precioAnterior, 
                           double precioNuevo, String fechaHora, String motivo, 
                           int idUsuario, String nombreUsuario) {
            this.idCambio = idCambio;
            this.producto = producto;
            this.precioAnterior = precioAnterior;
            this.precioNuevo = precioNuevo;
            this.fechaHora = fechaHora;
            this.motivo = motivo;
            this.idUsuario = idUsuario;
            this.nombreUsuario = nombreUsuario;
        }
        
        // Método para guardar en base de datos
        public boolean guardarEnBD() {
            String sql = "INSERT INTO historial_precios (id_producto, precio_anterior, " +
                        "precio_nuevo, fecha_hora, motivo, id_usuario) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (Connection cn = Conexion.realizarConexion();
                 PreparedStatement ps = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                
                ps.setInt(1, producto.getIdProducto());
                ps.setDouble(2, precioAnterior);
                ps.setDouble(3, precioNuevo);
                ps.setString(4, fechaHora);
                ps.setString(5, motivo);
                ps.setInt(6, idUsuario);
                
                int filasAfectadas = ps.executeUpdate();
                
                if (filasAfectadas > 0) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        this.idCambio = rs.getInt(1);
                    }
                    return true;
                }
                return false;
                
            } catch (ClassNotFoundException | SQLException ex) {
                System.err.println("Error al guardar cambio de precio en BD: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }
        
        @Override
        public String toString() {
            return String.format("Producto: %s (ID: %d) | De: S/%.2f a S/%.2f | Fecha: %s | Motivo: %s | Usuario: %s",
                producto.getNombre(), producto.getIdProducto(), precioAnterior, 
                precioNuevo, fechaHora, motivo, nombreUsuario);
        }
        
        // Formato para tabla
        public Object[] toTableRow() {
            return new Object[] {
                idCambio,
                producto.getIdProducto(),
                producto.getNombre(),
                String.format("S/ %.2f", precioAnterior),
                String.format("S/ %.2f", precioNuevo),
                fechaHora,
                motivo,
                nombreUsuario
            };
        }
        
        // Formato detallado
        public String toDetailedString() {
            StringBuilder sb = new StringBuilder();
            sb.append("══════════════════════════════════════\n");
            sb.append("          CAMBIO DE PRECIO #").append(idCambio).append("\n");
            sb.append("══════════════════════════════════════\n");
            sb.append("Producto: ").append(producto.getNombre()).append("\n");
            sb.append("Código: ").append(producto.getCodigo()).append("\n");
            sb.append("ID Producto: ").append(producto.getIdProducto()).append("\n");
            sb.append("──────────────────────────────────────\n");
            sb.append("Precio anterior: S/").append(String.format("%.2f", precioAnterior)).append("\n");
            sb.append("Precio nuevo:    S/").append(String.format("%.2f", precioNuevo)).append("\n");
            sb.append("Diferencia:      S/").append(String.format("%.2f", (precioNuevo - precioAnterior))).append("\n");
            sb.append("Porcentaje:      ").append(String.format("%.2f%%", ((precioNuevo - precioAnterior) / precioAnterior) * 100)).append("\n");
            sb.append("──────────────────────────────────────\n");
            sb.append("Fecha: ").append(fechaHora).append("\n");
            sb.append("Motivo: ").append(motivo).append("\n");
            sb.append("Usuario: ").append(nombreUsuario).append("\n");
            sb.append("══════════════════════════════════════\n");
            return sb.toString();
        }
        
        // Getters
        public int getIdCambio() { return idCambio; }
        public Producto getProducto() { return producto; }
        public double getPrecioAnterior() { return precioAnterior; }
        public double getPrecioNuevo() { return precioNuevo; }
        public String getFechaHora() { return fechaHora; }
        public String getMotivo() { return motivo; }
        public int getIdUsuario() { return idUsuario; }
        public String getNombreUsuario() { return nombreUsuario; }
    }
    
    // Atributos de la clase principal
    private Stack<CambioPrecio> pilaCambios;
    private GestorSistema gestor;
    
    // Constructor modificado: recibe el gestor como parámetro
    public HistorialCambiosPrecio(GestorSistema gestor) {
        this.gestor = gestor;
        this.pilaCambios = new Stack<>();
        cargarHistorialDesdeBD();
    }
    
    private void cargarHistorialDesdeBD() {
        String sql = "SELECT hp.*, p.nombre_producto, p.codigo, u.nombre_completo " +
                    "FROM historial_precios hp " +
                    "INNER JOIN productos p ON hp.id_producto = p.id_productos " +
                    "INNER JOIN usuarios u ON hp.id_usuario = u.id_usuarios " +
                    "ORDER BY hp.fecha_hora DESC " +
                    "LIMIT 100";
        
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Producto producto = new Producto();
                producto.setIdProducto(rs.getInt("id_producto"));
                producto.setNombre(rs.getString("nombre_producto"));
                producto.setCodigo(rs.getString("codigo"));
                
                CambioPrecio cambio = new CambioPrecio(
                    rs.getInt("id_historial"),
                    producto,
                    rs.getDouble("precio_anterior"),
                    rs.getDouble("precio_nuevo"),
                    rs.getString("fecha_hora"),
                    rs.getString("motivo"),
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_completo")
                );
                
                pilaCambios.push(cambio);
            }
            
            System.out.println("Historial de precios cargado desde BD: " + pilaCambios.size() + " registros");
            
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al cargar historial de precios desde BD: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // Método para registrar un cambio de precio
    public boolean registrarCambioPrecio(Producto producto, double precioAnterior, 
                                        double precioNuevo, String motivo) {
        if (producto == null) {
            System.err.println("Error: Producto no puede ser nulo");
            return false;
        }
        
        if (precioAnterior == precioNuevo) {
            System.out.println("No hay cambio de precio para registrar");
            return false;
        }
        
        // Verificar si hay usuario actual
        if (gestor.getUsuarioActual() == null) {
            System.err.println("Error: No hay usuario logueado");
            return false;
        }
        
        // Obtener usuario actual del gestor
        int idUsuario = gestor.getUsuarioActual().getIdUsuario();
        String nombreUsuario = gestor.getUsuarioActual().getNombreCompleto();
        
        // Crear cambio de precio
        CambioPrecio cambio = new CambioPrecio(producto, precioAnterior, precioNuevo, 
                                              motivo, idUsuario, nombreUsuario);
        
        // Guardar en base de datos
        if (cambio.guardarEnBD()) {
            // Agregar a la pila (LIFO)
            pilaCambios.push(cambio);
            
            // Registrar en log
            System.out.println("Cambio de precio registrado: " + cambio.toString());
            System.out.println("ID Cambio: " + cambio.getIdCambio());
            
            // Mostrar notificación si está habilitado
            if ((Boolean)gestor.getConfiguracion("mostrar_historial")) {
                String mensaje = "📈 CAMBIO DE PRECIO REGISTRADO\n\n" +
                               "Producto: " + producto.getNombre() + "\n" +
                               "De: S/" + String.format("%.2f", precioAnterior) + "\n" +
                               "A: S/" + String.format("%.2f", precioNuevo) + "\n" +
                               "Diferencia: S/" + String.format("%.2f", (precioNuevo - precioAnterior)) + "\n" +
                               "Motivo: " + motivo + "\n" +
                               "Usuario: " + nombreUsuario;
                
                JOptionPane.showMessageDialog(null, mensaje, 
                    "Historial Actualizado", JOptionPane.INFORMATION_MESSAGE);
            }
            
            return true;
        }
        
        return false;
    }
    
    // Método para deshacer el último cambio de precio
    public CambioPrecio deshacerUltimoCambio() {
        if (estaVacia()) {
            System.out.println("No hay cambios para deshacer");
            return null;
        }
        
        CambioPrecio ultimoCambio = pilaCambios.pop();
        System.out.println("Deshaciendo cambio: " + ultimoCambio.toString());
        
        return ultimoCambio;
    }
    
    // Método para ver el último cambio sin deshacerlo
    public CambioPrecio verUltimoCambio() {
        if (estaVacia()) {
            return null;
        }
        return pilaCambios.peek();
    }
    
    // Método para obtener todos los cambios
    public List<CambioPrecio> obtenerTodosLosCambios() {
        return new ArrayList<>(pilaCambios);
    }
    
    // Método para buscar cambios por producto
    public List<CambioPrecio> buscarCambiosPorProducto(int idProducto) {
        List<CambioPrecio> resultados = new ArrayList<>();
        
        for (CambioPrecio cambio : pilaCambios) {
            if (cambio.getProducto().getIdProducto() == idProducto) {
                resultados.add(cambio);
            }
        }
        
        return resultados;
    }
    
    // Método para buscar cambios por rango de fecha
    public List<CambioPrecio> buscarCambiosPorFecha(String fechaInicio, String fechaFin) {
        List<CambioPrecio> resultados = new ArrayList<>();
        
        for (CambioPrecio cambio : pilaCambios) {
            String fechaCambio = cambio.getFechaHora();
            if (fechaCambio.compareTo(fechaInicio) >= 0 && fechaCambio.compareTo(fechaFin) <= 0) {
                resultados.add(cambio);
            }
        }
        
        return resultados;
    }
    
    // Método para obtener estadísticas
    public EstadisticasPrecios obtenerEstadisticas() {
        return new EstadisticasPrecios(this);
    }
    
    // Clase para estadísticas
    public class EstadisticasPrecios {
        private int totalCambios;
        private int aumentos;
        private int disminuciones;
        private double promedioAumento;
        private double promedioDisminucion;
        private Producto productoMasModificado;
        
        public EstadisticasPrecios(HistorialCambiosPrecio historial) {
            calcularEstadisticas(historial);
        }
        
        private void calcularEstadisticas(HistorialCambiosPrecio historial) {
            totalCambios = historial.pilaCambios.size();
            aumentos = 0;
            disminuciones = 0;
            double sumaAumentos = 0;
            double sumaDisminuciones = 0;
            
            // Contar productos modificados
            Map<Integer, Integer> contadorProductos = new HashMap<>();
            
            for (CambioPrecio cambio : historial.pilaCambios) {
                // Contar aumentos/disminuciones
                if (cambio.getPrecioNuevo() > cambio.getPrecioAnterior()) {
                    aumentos++;
                    sumaAumentos += (cambio.getPrecioNuevo() - cambio.getPrecioAnterior());
                } else if (cambio.getPrecioNuevo() < cambio.getPrecioAnterior()) {
                    disminuciones++;
                    sumaDisminuciones += (cambio.getPrecioAnterior() - cambio.getPrecioNuevo());
                }
                
                // Contar modificaciones por producto
                int idProducto = cambio.getProducto().getIdProducto();
                contadorProductos.put(idProducto, contadorProductos.getOrDefault(idProducto, 0) + 1);
            }
            
            // Calcular promedios
            promedioAumento = aumentos > 0 ? sumaAumentos / aumentos : 0;
            promedioDisminucion = disminuciones > 0 ? sumaDisminuciones / disminuciones : 0;
            
            // Encontrar producto más modificado
            int maxModificaciones = 0;
            int idProductoMasModificado = 0;
            
            for (Map.Entry<Integer, Integer> entry : contadorProductos.entrySet()) {
                if (entry.getValue() > maxModificaciones) {
                    maxModificaciones = entry.getValue();
                    idProductoMasModificado = entry.getKey();
                }
            }
            
            // Buscar el producto más modificado
            if (idProductoMasModificado > 0) {
                for (CambioPrecio cambio : historial.pilaCambios) {
                    if (cambio.getProducto().getIdProducto() == idProductoMasModificado) {
                        productoMasModificado = cambio.getProducto();
                        break;
                    }
                }
            }
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("══════════════════════════════════════\n");
            sb.append("      ESTADÍSTICAS DE PRECIOS        \n");
            sb.append("══════════════════════════════════════\n");
            sb.append("Total cambios registrados: ").append(totalCambios).append("\n");
            sb.append("Aumentos de precio:        ").append(aumentos).append("\n");
            sb.append("Disminuciones de precio:   ").append(disminuciones).append("\n");
            sb.append("──────────────────────────────────────\n");
            sb.append("Promedio aumento:    S/").append(String.format("%.2f", promedioAumento)).append("\n");
            sb.append("Promedio disminución: S/").append(String.format("%.2f", promedioDisminucion)).append("\n");
            sb.append("──────────────────────────────────────\n");
            if (productoMasModificado != null) {
                sb.append("Producto más modificado: ").append(productoMasModificado.getNombre()).append("\n");
                sb.append("ID Producto: ").append(productoMasModificado.getIdProducto()).append("\n");
            }
            sb.append("══════════════════════════════════════\n");
            return sb.toString();
        }
        
        // Getters
        public int getTotalCambios() { return totalCambios; }
        public int getAumentos() { return aumentos; }
        public int getDisminuciones() { return disminuciones; }
        public double getPromedioAumento() { return promedioAumento; }
        public double getPromedioDisminucion() { return promedioDisminucion; }
        public Producto getProductoMasModificado() { return productoMasModificado; }
    }
    
    // Métodos de utilidad
    public boolean estaVacia() {
        return pilaCambios.isEmpty();
    }
    
    public int cantidadCambios() {
        return pilaCambios.size();
    }
    
    public void limpiarHistorial() {
        pilaCambios.clear();
        System.out.println("Historial de cambios limpiado (solo en memoria)");
    }
    
    // Método para exportar a texto
    public String exportarATexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("     HISTORIAL DE CAMBIOS DE PRECIO   \n");
        sb.append("══════════════════════════════════════\n");
        sb.append("Fecha de exportación: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("Total de registros: ").append(pilaCambios.size()).append("\n");
        sb.append("══════════════════════════════════════\n\n");
        
        int contador = 1;
        for (CambioPrecio cambio : pilaCambios) {
            sb.append("REGISTRO #").append(contador++).append("\n");
            sb.append(cambio.toDetailedString()).append("\n");
        }
        
        return sb.toString();
    }
}