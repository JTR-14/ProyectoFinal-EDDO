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
    
    // Constructor privado
    private GestorSistema() {
        configuraciones = new HashMap<>();
        cargarConfiguracionesDefault();
        // NO inicializar aquí los componentes que dependen del gestor
    }
    
    // Método estático para obtener la instancia única
    public static GestorSistema getInstancia() {
        if (instancia == null) {
            instancia = new GestorSistema();
        }
        return instancia;
    }
    
    // Carga configuraciones por defecto
    private void cargarConfiguracionesDefault() {
        configuraciones.put("notificar_stock_minimo", true);
        configuraciones.put("auto_completar", true);
        configuraciones.put("mostrar_historial", true);
        configuraciones.put("iva_porcentaje", 0.18);
    }
    
    // Getters con inicialización perezosa (Lazy Initialization)
    
    public ColaPedidosOnline getColaPedidos() {
        if (colaPedidos == null) {
            colaPedidos = new ColaPedidosOnline();
        }
        return colaPedidos;
    }
    
    public HistorialCambiosPrecio getHistorialPrecios() {
        if (historialPrecios == null) {
            historialPrecios = new HistorialCambiosPrecio(this);
        }
        return historialPrecios;
    }
    
    public HistorialNavegacion getHistorialNavegacion() {
        if (historialNavegacion == null) {
            historialNavegacion = new HistorialNavegacion();
        }
        return historialNavegacion;
    }
    
    // Getters y Setters básicos
    
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
    
    // Métodos de utilidad
    
    public double getIVA() {
        return (double) configuraciones.getOrDefault("iva_porcentaje", 0.18);
    }
    
    public void limpiarSesion() {
        usuarioActual = null;
        historialNavegacion = new HistorialNavegacion();
    }
}