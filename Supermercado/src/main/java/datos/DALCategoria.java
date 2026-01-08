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
import modelo.Categoria;

/**
 *
 * @author Toledo
 */

public class DALCategoria {

    public static ArrayList<Categoria> listarCategorias() {
        ArrayList<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM Categorias";
        try (Connection cn = Conexion.realizarConexion(); 
                PreparedStatement ps = cn.prepareStatement(sql); 
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id_categorias"),
                        rs.getString("nombre_categoria")));
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al listar categorías: " + ex.getMessage());
            ex.printStackTrace();
        }
        return lista;
    }
    
    public static boolean insertarCategoria(String nombreCategoria){
        if(buscarRepetido(nombreCategoria)){
            JOptionPane.showMessageDialog(null, "Esta categoría ya se encuentra en la Base de Datos", "Mensaje", 2);
            return false;
        }
        String sql = "INSERT INTO Categorias (nombre_categoria) VALUES (?)";
        try(Connection cn = Conexion.realizarConexion();
                PreparedStatement ps = cn.prepareStatement(sql)){
                ps.setString(1, nombreCategoria);
                return ps.executeUpdate() > 0;
                
        }catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al insertar categoría: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    public static boolean modificarCategoria(int id, String nombre) {
        String sql = "UPDATE Categorias SET nombre_categoria=? WHERE id_categorias=?";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al modificar categoría: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean eliminarCategoria(int id) {
        String sql = "DELETE FROM Categorias WHERE id_categorias=?";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al eliminar categoría: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    private static boolean buscarRepetido(String categoria){
        String sql = "SELECT nombre_categoria FROM Categorias WHERE nombre_categoria = ?";
        try(Connection cn = Conexion.realizarConexion();
                PreparedStatement ps = cn.prepareStatement(sql)){
            ps.setString(1, categoria);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al buscar categoría repetida: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
}
