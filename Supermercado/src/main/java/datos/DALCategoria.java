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
        String sql = "SELECT * FROM categorias";
        try (Connection cn = Conexion.realizarConexion(); 
                PreparedStatement ps = cn.prepareStatement(sql); 
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id_categorias"),
                        rs.getString("nombre_categoria")));
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al insertar producto: " + ex.getMessage());
        }
        return lista;
    }
    public static boolean insertarCategoria(String nombreProducto){
        if(buscarRepetido(nombreProducto)){
            JOptionPane.showMessageDialog(null, "Esta categoria ya se encuentra en la Base de Datos", "Mensaje",2);
            return false;
        }
        String sql = "INSERT INTO (nombre_categoria) VALUES (?)";
        try(Connection cn = Conexion.realizarConexion();
                PreparedStatement ps = cn.prepareStatement(sql)){
                ps.setString(1, nombreProducto);
                return ps.executeUpdate()>0;
                
        }catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al insertar producto: " + ex.getMessage());
            return false;
        }
    }
    public static boolean modificarCategoria(int id, String nombre) {
        String sql = "UPDATE categorias SET nombre_categoria=? WHERE id_categorias=?";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al modificar categoría: " + ex.getMessage());
            return false;
        }
    }

    public static boolean eliminarCategoria(int id) {
        String sql = "DELETE FROM categorias WHERE id_categorias=?";

        try (Connection cn = Conexion.realizarConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al eliminar categoría: " + ex.getMessage());
            return false;
        }
    }
    
    private static boolean buscarRepetido(String categoria){
        String sql = "SELECT nombre_categoria FROM categorias WHERE nombre_categoria = ?";
        try(Connection cn = Conexion.realizarConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ){
            ps.setString(1, categoria);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al eliminar categoría: " + ex.getMessage());
            return false;
        }
    }
}
