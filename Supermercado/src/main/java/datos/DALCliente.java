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
import javax.swing.JOptionPane;
import modelo.Cliente;

/**
 *
 * @author Toledo
 */
public class DALCliente {
    private static boolean existeCliente(String dni) {
        String sql = "SELECT id_cliente FROM clientes WHERE dni_ruc = ?";
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }
        } catch (SQLException | ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean registrarCliente(String nombre, String dni, String telefono, String direccion) {
        if (existeCliente(dni)) {
            JOptionPane.showMessageDialog(null, "El DNI le pertenece a otra persona","AVISO",3);
            return false;
        }

        String sql = "INSERT INTO clientes (nombre_cliente, dni_cliente, telefono, direccion) VALUES (?,?,?,?)";
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono);
            ps.setString(4, direccion);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException ex) {
            System.err.println("Error al registrar cliente: " + ex.getMessage());
            return false;
        }
    }

    public static boolean modificarCliente(int id, String nombre, String dni, String telefono, String direccion) {
        String sql = "UPDATE clientes SET nombre_cliente=?, dni_cliente=?, telefono=?, direccion=? WHERE id_clientes=?";
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono);
            ps.setString(4, direccion);
            ps.setInt(5, id);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException ex) {
            System.err.println("Error al modificar cliente: " + ex.getMessage());
            return false;
        }
    }


    public static boolean eliminarCliente(int id) {
        String sql = "DELETE FROM clientes WHERE id_clientes=?";
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException ex) {
            System.err.println("Error al eliminar (Puede tener ventas): " + ex.getMessage());
            return false;
        }
    }


    public static ArrayList<Cliente> obtenerClientes() {
        ArrayList<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";
        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getInt("id_clientes"),
                    rs.getString("nombre_cliente"),
                    rs.getString("dni_cliente"),
                    rs.getString("telefono"),
                    rs.getString("direccion")
                ));
            }
        } catch (Exception ex) {
            System.err.println("Error al listar: " + ex.getMessage());
        }
        return lista;
    }
}

