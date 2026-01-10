package logica;

import datos.DALProductos;
import utiles.ArbolAVL;
import modelo.Producto;
import java.util.ArrayList;

public class ControladorProductos {

    private static ArbolAVL<Producto> arbolProductos = new ArbolAVL<>();

    public static void cargarDatosAlArbol() {
        arbolProductos.limpiar();
        ArrayList<Producto> lista = DALProductos.obtenerProductos();
        
        for (Producto p : lista) {
            arbolProductos.insertar(p);
        }
        System.out.println("Árbol Genérico cargado con " + lista.size() + " productos.");
    }

    public static void agregarAlArbol(Producto p) {
        arbolProductos.insertar(p);
    }

    public static Producto buscarPorCodigo(String codigo) {
        Producto moldeBusqueda = new Producto(codigo);
        return arbolProductos.buscar(moldeBusqueda);
    }
}