/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datos;
/**
 *
 * @author Toledo
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Usuario;


public class DALUsuarios {
    public static Usuario login(String username, String contraseña) {
        Usuario usuarioEncontrado = null;

        String sql = "SELECT * FROM Usuarios WHERE username = ? AND contraseña = ?";

        try (Connection con = Conexion.realizarConexion();
                PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setString(1, username);
            ps.setString(2, contraseña);
            
            try(ResultSet rs = ps.executeQuery()){

            if (rs.next()) {

                usuarioEncontrado = new Usuario(
                rs.getInt("id_usuarios"),
                rs.getString("nombre_completo"),
                rs.getString("username"),
                rs.getString("contraseña"),
                rs.getInt("id_rol"));
            }
        }
        }catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Error al iniciar sesión: " + ex.getMessage());
        }
        return usuarioEncontrado;
}
    
}

