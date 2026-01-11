package logica;

import datos.DALProductos;
import utiles.ArbolAVL;
import modelo.Producto;
import java.util.ArrayList;

public class ControladorProductos {

    private static ArbolAVL<Producto> arbolProductos = new ArbolAVL<>();
    private static boolean datosCargados = false;

    public static void cargarDatosAlArbol() {
        if (!datosCargados) {
            arbolProductos.limpiar();
            ArrayList<Producto> lista = DALProductos.obtenerProductos();
            
            for (Producto p : lista) {
                arbolProductos.insertar(p);
            }
            datosCargados = true;
            System.out.println("Árbol AVL cargado con " + lista.size() + " productos.");
        }
    }

    public static void agregarAlArbol(Producto p) {
        arbolProductos.insertar(p);
        datosCargados = true; // Marcar como cargado
    }

    public static Producto buscarPorCodigo(String codigo) {
        // Asegurar que el árbol esté cargado
        if (!datosCargados) {
            cargarDatosAlArbol();
        }
        
        Producto moldeBusqueda = new Producto(codigo);
        return arbolProductos.buscar(moldeBusqueda);
    }
    
    // NUEVO: Método para sincronizar cambios
    public static void actualizarProductoEnArbol(Producto productoActualizado) {
        // Buscar y actualizar el producto en el árbol
        Producto existente = buscarPorCodigo(productoActualizado.getCodigo());
        if (existente != null) {
            // Actualizar los datos del producto existente
            existente.setNombre(productoActualizado.getNombre());
            existente.setPrecioCosto(productoActualizado.getPrecioCosto());
            existente.setPrecioVenta(productoActualizado.getPrecioVenta());
            existente.setStockActual(productoActualizado.getStockActual());
            existente.setStockMinimo(productoActualizado.getStockMinimo());
            existente.setIdCategoria(productoActualizado.getIdCategoria());
        } else {
            // Si no existe, agregarlo
            agregarAlArbol(productoActualizado);
        }
    }
    
    // NUEVO: Forzar recarga del árbol
    public static void forzarRecarga() {
        datosCargados = false;
        cargarDatosAlArbol();
        System.out.println("Árbol AVL forzado a recargar datos desde BD.");
    }
    
    // NUEVO: Verificar si el árbol está cargado
    public static boolean isDatosCargados() {
        return datosCargados;
    }
    
    public static void actualizarStockProducto(String codigo, int cantidadVendida) {
        Producto producto = buscarPorCodigo(codigo);
        if (producto != null) {
            int nuevoStock = producto.getStockActual() - cantidadVendida;
            if (nuevoStock < 0) nuevoStock = 0;
            producto.setStockActual(nuevoStock);
            actualizarProductoEnArbol(producto);
            
            // También actualizar en la base de datos
            DALProductos.modificarProducto(
                producto.getIdProducto(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecioCosto(),
                producto.getPrecioVenta(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getIdCategoria()
            );
        }
    }
}