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

}
