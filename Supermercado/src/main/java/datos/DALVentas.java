package datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;



import modelo.DetalleVenta;
import modelo.Venta;
import modelo.Producto;
import utiles.ListaEnlazadaDoble;
import utiles.NodoEnDoble;

public class DALVentas {


    public static int registrarVenta(Venta venta) {
        // USAR la estructura ORIGINAL de la tabla (sin subtotal e igv)
        String sqlEncabezado = 
            "INSERT INTO Ventas_Encabezado (fecha_hora, monto_total, id_usuarios, id_clientes) " +
            "VALUES (?, ?, ?, ?)";
    
        Connection cn = null;
        PreparedStatement psEncabezado = null;
        ResultSet rs = null;
    
        try {
            cn = Conexion.realizarConexion();
            cn.setAutoCommit(false);
    
            psEncabezado = cn.prepareStatement(
                sqlEncabezado,
                Statement.RETURN_GENERATED_KEYS
            );
    
            Timestamp fechaTimestamp = Timestamp.valueOf(venta.getFecha());
    
            // Usar solo los campos que existen en la tabla
            psEncabezado.setTimestamp(1, fechaTimestamp);
            psEncabezado.setDouble(2, venta.getMontoTotal());
            psEncabezado.setInt(3, venta.getIdUsuario());
            psEncabezado.setInt(4, venta.getIdCliente());
    
            int filas = psEncabezado.executeUpdate();
    
            if (filas > 0) {
                rs = psEncabezado.getGeneratedKeys();
    
                if (rs.next()) {
                    int idVenta = rs.getInt(1);
    
                    if (registrarDetallesVenta(cn, idVenta, venta.getDetalles())
                        && actualizarStockProductos(cn, venta.getDetalles())) {
    
                        cn.commit();
                        return idVenta;
                    }
                }
            }
    
            cn.rollback();
            return 0;
    
        } catch (Exception ex) {
            try {
                if (cn != null) cn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ex.printStackTrace();
            return 0;
    
        } finally {
            try {
                if (rs != null) rs.close();
                if (psEncabezado != null) psEncabezado.close();
                if (cn != null) cn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static boolean registrarDetallesVenta(
            Connection cn,
            int idVenta,
            ListaEnlazadaDoble<DetalleVenta> detalles
    ) throws SQLException {

        String sql =
            "INSERT INTO Ventas_detalle " +
            "(cantidad, precio_unitario, subtotal, id_venta, id_productos) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            NodoEnDoble<DetalleVenta> p = detalles.getPrimero();

            while (p != null) {
                DetalleVenta d = p.getInfo();

                ps.setInt(1, d.getCantidad());
                ps.setDouble(2, d.getPrecioUnitario());
                ps.setDouble(3, d.getSubTotal());
                ps.setInt(4, idVenta);
                ps.setInt(5, d.getIdProducto());
                ps.addBatch();

                p = p.getSgte();
            }

            int[] resultados = ps.executeBatch();
            for (int r : resultados) {
                if (r <= 0) return false;
            }
            return true;
        }
    }

    private static boolean actualizarStockProductos(
            Connection cn,
            ListaEnlazadaDoble<DetalleVenta> detalles
    ) throws SQLException {

        String sql =
            "UPDATE Productos " +
            "SET stock_actual = stock_actual - ? " +
            "WHERE id_productos = ? AND stock_actual >= ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            NodoEnDoble<DetalleVenta> p = detalles.getPrimero();

            while (p != null) {
                DetalleVenta d = p.getInfo();

                ps.setInt(1, d.getCantidad());
                ps.setInt(2, d.getIdProducto());
                ps.setInt(3, d.getCantidad());
                ps.addBatch();

                p = p.getSgte();
            }

            int[] resultados = ps.executeBatch();
            for (int r : resultados) {
                if (r <= 0) return false;
            }
            return true;
        }
    }

    // CORRECCIÓN: Actualizar método para incluir subtotal e igv en la consulta
    public static ArrayList<Venta> obtenerVentasPorFecha(
            String fechaInicio,
            String fechaFin
    ) {

        ArrayList<Venta> lista = new ArrayList<>();

        String sql =
            "SELECT v.*, c.nombre_cliente, u.nombre_completo " +
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

                    // CORRECCIÓN: Cargar subtotal e igv si existen
                    try {
                        venta.setSubTotal(rs.getDouble("subtotal"));
                        venta.setIgv(rs.getDouble("igv"));
                    } catch (SQLException e) {
                        // Si las columnas no existen, calcularlos
                        venta.calcularMontoTotal();
                    }

                    venta.setNombreCliente(rs.getString("nombre_cliente"));
                    venta.setNombreUsuario(rs.getString("nombre_completo"));

                    lista.add(venta);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return lista;
    }

    // CORRECCIÓN: Actualizar método obtenerVentaPorId
    public static Venta obtenerVentaPorId(int idVenta) {

        String sql =
            "SELECT v.*, c.nombre_cliente, u.nombre_completo " +
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

                    // CORRECCIÓN: Cargar subtotal e igv
                    try {
                        venta.setSubTotal(rs.getDouble("subtotal"));
                        venta.setIgv(rs.getDouble("igv"));
                    } catch (SQLException e) {
                        venta.calcularMontoTotal();
                    }

                    venta.setNombreCliente(rs.getString("nombre_cliente"));
                    venta.setNombreUsuario(rs.getString("nombre_completo"));

                    ListaEnlazadaDoble<DetalleVenta> lista = new ListaEnlazadaDoble<>();
                    for (DetalleVenta d : obtenerDetallesVenta(idVenta)) {
                         lista.insertaAlFinal(d);
                    }

                    venta.setDetalles(lista);
                    return venta;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static ArrayList<DetalleVenta> obtenerDetallesVenta(int idVenta) {

        ArrayList<DetalleVenta> detalles = new ArrayList<>();

        String sql =
            "SELECT d.*, p.nombre_producto, p.codigo " +
            "FROM Ventas_detalle d " +
            "INNER JOIN Productos p ON d.id_productos = p.id_productos " +
            "WHERE d.id_venta = ?";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    DetalleVenta d = new DetalleVenta(
                        rs.getInt("id_venta"),
                        rs.getInt("id_productos"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal")
                    );

                    Producto p = new Producto();
                    p.setIdProducto(rs.getInt("id_productos"));
                    p.setNombre(rs.getString("nombre_producto"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setPrecioVenta(rs.getDouble("precio_unitario"));

                    d.setProducto(p);
                    detalles.add(d);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return detalles;
    }


    public static int obtenerSiguienteNumeroVenta() {

        String sql = "SELECT MAX(id_venta) AS ultimo FROM Ventas_Encabezado";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("ultimo") + 1;
            }
            return 1;

        } catch (Exception ex) {
            ex.printStackTrace();
            return 1;
        }
    }
}
