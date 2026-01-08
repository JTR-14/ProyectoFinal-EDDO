/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datos;

/**
 *
 * @author USER
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import modelo.DetalleVenta;
import modelo.Venta;

public class DALVentas {
    
    // Registrar una venta completa con sus detalles
    public static int registrarVenta(Venta venta) {
        String sqlEncabezado = "INSERT INTO Ventas_Encabezado (fecha_hora, monto_total, id_usuarios, id_clientes) VALUES (?, ?, ?, ?)";
        
        Connection cn = null;
        PreparedStatement psEncabezado = null;
        ResultSet rs = null;
        
        try {
            cn = Conexion.realizarConexion();
            cn.setAutoCommit(false); // Iniciar transacción
            
            // Registrar encabezado de venta
            psEncabezado = cn.prepareStatement(sqlEncabezado, Statement.RETURN_GENERATED_KEYS);
            
            // Convertir fecha string a Timestamp
            Timestamp fechaTimestamp = Timestamp.valueOf(venta.getFecha());
            
            psEncabezado.setTimestamp(1, fechaTimestamp);
            psEncabezado.setDouble(2, venta.getMontoTotal());
            psEncabezado.setInt(3, venta.getIdUsuario());
            psEncabezado.setInt(4, venta.getIdCliente());
            
            int filasAfectadas = psEncabezado.executeUpdate();
            
            if (filasAfectadas > 0) {
                // Obtener el ID de la venta generada
                rs = psEncabezado.getGeneratedKeys();
                int idVenta = 0;
                if (rs.next()) {
                    idVenta = rs.getInt(1);
                    
                    // Registrar detalles de la venta
                    if (registrarDetallesVenta(cn, idVenta, venta.getDetalles())) {
                        // Actualizar stock de productos
                        if (actualizarStockProductos(cn, venta.getDetalles())) {
                            cn.commit(); // Confirmar transacción
                            return idVenta;
                        } else {
                            cn.rollback(); // Revertir si hay error en stock
                            return 0;
                        }
                    } else {
                        cn.rollback(); // Revertir si hay error en detalles
                        return 0;
                    }
                }
            }
            
            cn.rollback();
            return 0;
            
        } catch (ClassNotFoundException | SQLException ex) {
            try {
                if (cn != null) cn.rollback();
            } catch (SQLException e) {
                System.err.println("Error al hacer rollback: " + e.getMessage());
            }
            System.err.println("Error al registrar venta: " + ex.getMessage());
            ex.printStackTrace();
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (psEncabezado != null) psEncabezado.close();
                if (cn != null) cn.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar recursos: " + ex.getMessage());
            }
        }
    }
    
    private static boolean registrarDetallesVenta(Connection cn, int idVenta, ArrayList<DetalleVenta> detalles) throws SQLException {
        String sqlDetalle = "INSERT INTO Ventas_detalle (cantidad, precio_unitario, subtotal, id_venta, id_productos) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement psDetalle = cn.prepareStatement(sqlDetalle)) {
            for (DetalleVenta detalle : detalles) {
                psDetalle.setInt(1, detalle.getCantidad());
                psDetalle.setDouble(2, detalle.getPrecioUnitario());
                psDetalle.setDouble(3, detalle.getSubtotal());
                psDetalle.setInt(4, idVenta);
                psDetalle.setInt(5, detalle.getIdProducto());
                psDetalle.addBatch();
            }
            
            int[] resultados = psDetalle.executeBatch();
            for (int resultado : resultados) {
                if (resultado <= 0) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private static boolean actualizarStockProductos(Connection cn, ArrayList<DetalleVenta> detalles) throws SQLException {
        String sqlActualizarStock = "UPDATE Productos SET stock_actual = stock_actual - ? WHERE id_productos = ? AND stock_actual >= ?";
        
        try (PreparedStatement ps = cn.prepareStatement(sqlActualizarStock)) {
            for (DetalleVenta detalle : detalles) {
                ps.setInt(1, detalle.getCantidad());
                ps.setInt(2, detalle.getIdProducto());
                ps.setInt(3, detalle.getCantidad());
                ps.addBatch();
            }
            
            int[] resultados = ps.executeBatch();
            for (int resultado : resultados) {
                if (resultado <= 0) {
                    return false;
                }
            }
            return true;
        }
    }
    
    // Obtener ventas por rango de fecha
    public static ArrayList<Venta> obtenerVentasPorFecha(String fechaInicio, String fechaFin) {
        ArrayList<Venta> lista = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre_cliente, u.nombre_completo " +
                     "FROM Ventas_Encabezado v " +
                     "INNER JOIN Clientes c ON v.id_clientes = c.id_clientes " +
                     "INNER JOIN Usuarios u ON v.id_usuarios = u.id_usuarios " +
                     "WHERE v.fecha_hora BETWEEN ? AND ? " +
                     "ORDER BY v.fecha_hora DESC";
        
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta venta = new Venta(
                        rs.getInt("id_venta"),
                        rs.getString("fecha_hora"),
                        rs.getDouble("monto_total"),
                        rs.getInt("id_usuarios"),
                        rs.getInt("id_clientes")
                    );
                    venta.setNombreCliente(rs.getString("nombre_cliente"));
                    venta.setNombreUsuario(rs.getString("nombre_completo"));
                    lista.add(venta);
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al obtener ventas: " + ex.getMessage());
            ex.printStackTrace();
        }
        return lista;
    }
    
    // Obtener detalles de una venta específica
    public static ArrayList<DetalleVenta> obtenerDetallesVenta(int idVenta) {
        ArrayList<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT d.*, p.nombre_producto, p.codigo " +
                     "FROM Ventas_detalle d " +
                     "INNER JOIN Productos p ON d.id_productos = p.id_productos " +
                     "WHERE d.id_venta = ?";
        
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            
            ps.setInt(1, idVenta);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta detalle = new DetalleVenta(
                        rs.getInt("id_venta"),
                        rs.getInt("id_productos"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal")
                    );
                    detalle.setIdDetalle(rs.getInt("id_detalle"));
                    
                    // Crear producto básico para mostrar información
                    modelo.Producto producto = new modelo.Producto();
                    producto.setIdProducto(rs.getInt("id_productos"));
                    producto.setNombre(rs.getString("nombre_producto"));
                    producto.setCodigo(rs.getString("codigo"));
                    producto.setPrecioVenta(rs.getDouble("precio_unitario"));
                    
                    detalle.setProducto(producto);
                    detalles.add(detalle);
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al obtener detalles de venta: " + ex.getMessage());
            ex.printStackTrace();
        }
        return detalles;
    }
    
    // Obtener venta por ID
    public static Venta obtenerVentaPorId(int idVenta) {
        String sql = "SELECT v.*, c.nombre_cliente, u.nombre_completo " +
                     "FROM Ventas_Encabezado v " +
                     "INNER JOIN Clientes c ON v.id_clientes = c.id_clientes " +
                     "INNER JOIN Usuarios u ON v.id_usuarios = u.id_usuarios " +
                     "WHERE v.id_venta = ?";
        
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            
            ps.setInt(1, idVenta);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Venta venta = new Venta(
                        rs.getInt("id_venta"),
                        rs.getString("fecha_hora"),
                        rs.getDouble("monto_total"),
                        rs.getInt("id_usuarios"),
                        rs.getInt("id_clientes")
                    );
                    venta.setNombreCliente(rs.getString("nombre_cliente"));
                    venta.setNombreUsuario(rs.getString("nombre_completo"));
                    
                    // Obtener detalles
                    venta.setDetalles(obtenerDetallesVenta(idVenta));
                    
                    return venta;
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al obtener venta por ID: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    
    // Generar número de comprobante (siguiente número disponible)
    public static int obtenerSiguienteNumeroVenta() {
        String sql = "SELECT MAX(id_venta) as ultimo_numero FROM Ventas_Encabezado";
        
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("ultimo_numero") + 1;
            }
            return 1; // Primera venta
            
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al obtener siguiente número de venta: " + ex.getMessage());
            ex.printStackTrace();
            return 1;
        }
    }
}