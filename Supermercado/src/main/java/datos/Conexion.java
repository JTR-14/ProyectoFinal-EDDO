/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Conexion {
    public static Connection realizarConexion() throws ClassNotFoundException, SQLException {
        String url, user, password;
        Class.forName("com.mysql.cj.jdbc.Driver");
        url = "jdbc:mysql://localhost:3306/supermercado";
        user = "root";
        password = "natali19";
        return DriverManager.getConnection(url, user, password);
    } 
    public static void main(String[] args) {
        try {
            Connection c = realizarConexion();
            if (c != null) {
                System.out.println("¡CONEXIÓN EXITOSA A LA BASE DE DATOS SUPERMERCADO!");
                c.close();
            }
        } catch (Exception e) {
            System.out.println("Error al conectar: " + e.getMessage());
        }
    }
}
