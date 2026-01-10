package logica;

import datos.DALProductos;
import utiles.ArbolProductosAVL;
import modelo.Producto;
import java.util.ArrayList;

public class ControladorProductos {

    private static ArbolProductosAVL arbolProductos = new ArbolProductosAVL();
        //Mas rapidez en busquedas
    public static void cargarDatosAlArbol() {
        arbolProductos = new ArbolProductosAVL(); 
        ArrayList<Producto> listaBD = DALProductos.obtenerProductos();

        for (Producto p : listaBD) {
            arbolProductos.insertar(p);
        }
        
        System.out.println("Carga completada: " + listaBD.size() + " productos en memoria RAM.");
    }

    public static Producto buscarPorCodigo(String codigo) {
        return arbolProductos.buscar(codigo);
    }

    public static void agregarAlArbol(Producto p) {
        arbolProductos.insertar(p);
    }
}