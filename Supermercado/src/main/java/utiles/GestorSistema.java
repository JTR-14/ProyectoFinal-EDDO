/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

/**
 *
 * @author USER
 */

import java.util.HashMap;
import java.util.Map;
import modelo.Usuario;

public class GestorSistema {
    private static GestorSistema instancia;
    private ColaPedidosOnline colaPedidos;
    private HistorialCambiosPrecio historialPrecios;
    private HistorialNavegacion historialNavegacion;
    private Map<String, Object> configuraciones;
    private Usuario usuarioActual;
    
    private GestorSistema() {
        colaPedidos = new ColaPedidosOnline();
        historialPrecios = new HistorialCambiosPrecio();
        historialNavegacion = new HistorialNavegacion();
        configuraciones = new HashMap<>();
        cargarConfiguracionesDefault();
    }
    
    public static GestorSistema getInstancia() {
        if (instancia == null) {
            instancia = new GestorSistema();
        }
        return instancia;
    }
    
    private void cargarConfiguracionesDefault() {
        configuraciones.put("notificar_stock_minimo", true);
        configuraciones.put("auto_completar", true);
        configuraciones.put("mostrar_historial", true);
    }
    
    // Getters para las estructuras de datos
    public ColaPedidosOnline getColaPedidos() {
        return colaPedidos;
    }
    
    public HistorialCambiosPrecio getHistorialPrecios() {
        return historialPrecios;
    }
    
    public HistorialNavegacion getHistorialNavegacion() {
        return historialNavegacion;
    }
    
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    public Object getConfiguracion(String clave) {
        return configuraciones.get(clave);
    }
    
    public void setConfiguracion(String clave, Object valor) {
        configuraciones.put(clave, valor);
    }
}