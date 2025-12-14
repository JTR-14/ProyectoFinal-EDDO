/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Producto;

public class DALProductos {
    public static boolean insertarProducto(String codigo, String nombre, double precioCosto, double precioVenta,
            int stockActual, int stockMinimo, int idCategoria){
        String sql = "INSERT INTO productos (codigo, nombre_producto, precio_costo, precio_venta, stock_actual, stock_minimo, id_categorias) VALUES (?,?,?,?,?,?,?) ";
        try(Connection cn = Conexion.realizarConexion();
               PreparedStatement ps = cn.prepareStatement(sql)){
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setDouble(3, precioCosto);
            ps.setDouble(4, precioVenta);
            ps.setInt(5, stockActual);
            ps.setInt(6, stockMinimo);
            ps.setInt(7, idCategoria);
            return ps.executeUpdate()>0;
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al insertar producto: " + ex.getMessage());
            return false;
        }
    }

    public static boolean modificarProducto(int idProducto, String codigo, String nombre, double precioCosto, double precioVenta,
            int stockActual, int stockMinimo, int idCategoria) {
            
        String sql = "UPDATE productos SET codigo=?, nombre_producto=?, precio_costo=?, precio_venta=?, stock_actual=?, stock_minimo=?, id_categorias=? WHERE id_productos=?";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setDouble(3, precioCosto);
            ps.setDouble(4, precioVenta);
            ps.setInt(5, stockActual);
            ps.setInt(6, stockMinimo);
            ps.setInt(7, idCategoria);
            
            ps.setInt(8, idProducto);

            return ps.executeUpdate() > 0;

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al modificar producto: " + ex.getMessage());
            return false;
        }
    }


    public static ArrayList<Producto> obtenerProductos() {
        ArrayList<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                
                lista.add(new Producto(
                    rs.getInt("id_productos"),
                    rs.getString("codigo"),
                    rs.getString("nombre_producto"),
                    rs.getDouble("precio_costo"),
                    rs.getDouble("precio_venta"),
                    rs.getInt("stock_actual"),
                    rs.getInt("stock_minimo"),
                    rs.getInt("id_categorias")
                ));
            }

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al listar productos: " + ex.getMessage());
        }
        return lista;
    }

    // 4. MÉTODO BUSCAR (Por ID - Vital para cuando seleccionas una fila de la tabla)
    public static Producto buscarProducto(int id) {
        String sql = "SELECT * FROM productos WHERE id_productos = ?";
        Producto prod = null;

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prod = new Producto(
                        rs.getInt("id_productos"),
                        rs.getString("codigo"),
                        rs.getString("nombre_producto"),
                        rs.getDouble("precio_costo"),
                        rs.getDouble("precio_venta"),
                        rs.getInt("stock_actual"),
                        rs.getInt("stock_minimo"),
                        rs.getInt("id_categorias")
                    );
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al buscar producto: " + ex.getMessage());
        }
        return prod;
    }

    // 5. MÉTODO ELIMINAR (Usamos ID por seguridad)
    public static boolean eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id_productos = ?";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al eliminar producto: " + ex.getMessage());
            return false;
        }
    }
    
    // 6. EXTRA: BUSCAR POR CÓDIGO DE BARRAS (Útil para la pantalla de Ventas)
    public static Producto buscarPorCodigo(String codigoBarras) {
        String sql = "SELECT * FROM productos WHERE codigo = ?";
        Producto prod = null;

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codigoBarras);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prod = new Producto(
                        rs.getInt("id_productos"),
                        rs.getString("codigo"),
                        rs.getString("nombre_producto"),
                        rs.getDouble("precio_costo"),
                        rs.getDouble("precio_venta"),
                        rs.getInt("stock_actual"),
                        rs.getInt("stock_minimo"),
                        rs.getInt("id_categorias")
                    );
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al buscar por código: " + ex.getMessage());
        }
        return prod;
    }
}

