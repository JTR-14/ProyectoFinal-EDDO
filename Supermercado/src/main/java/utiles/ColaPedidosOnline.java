/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

/**
 *
 * @author USER
 */
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import modelo.Pedido;

public class ColaPedidosOnline {
    private Queue<Pedido> colaPedidos;
    private int siguienteId;
    private ArrayList<Pedido> historialPedidos;
    
    public ColaPedidosOnline() {
        colaPedidos = new LinkedList<>();
        historialPedidos = new ArrayList<>();
        siguienteId = 1;
    }
    
    // Método para agregar un nuevo pedido a la cola (FIFO)
    public boolean agregarPedido(Pedido pedido) {
        if (pedido == null) {
            JOptionPane.showMessageDialog(null, "El pedido no puede ser nulo", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (pedido.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El pedido debe contener al menos un producto", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Verificar stock disponible
        if (!pedido.verificarStockDisponible()) {
            JOptionPane.showMessageDialog(null, "No hay stock suficiente para algunos productos", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Asignar ID único
        pedido.setIdPedido(siguienteId++);
        
        // Agregar a la cola
        boolean agregado = colaPedidos.offer(pedido);
        
        if (agregado) {
            historialPedidos.add(pedido);
            System.out.println("Pedido #" + pedido.getIdPedido() + " agregado a la cola");
        }
        
        return agregado;
    }
    
    // Método para procesar el siguiente pedido (FIFO)
    public Pedido procesarSiguientePedido() {
        if (estaVacia()) {
            System.out.println("No hay pedidos pendientes en la cola");
            return null;
        }
        
        Pedido pedidoProcesado = colaPedidos.poll();
        pedidoProcesado.setEstado("EN_PROCESO");
        
        System.out.println("Procesando pedido #" + pedidoProcesado.getIdPedido() + 
                          " - Cliente: " + pedidoProcesado.getCliente().getNombre());
        
        return pedidoProcesado;
    }
    
    // Método para completar un pedido procesado
    public boolean completarPedido(Pedido pedido) {
        if (pedido != null && pedido.getEstado().equals("EN_PROCESO")) {
            pedido.setEstado("COMPLETADO");
            System.out.println("Pedido #" + pedido.getIdPedido() + " completado");
            return true;
        }
        return false;
    }
    
    // Método para cancelar un pedido
    public boolean cancelarPedido(Pedido pedido) {
        if (pedido != null) {
            pedido.setEstado("CANCELADO");
            
            // Si está en la cola, quitarlo
            if (colaPedidos.contains(pedido)) {
                colaPedidos.remove(pedido);
            }
            
            System.out.println("Pedido #" + pedido.getIdPedido() + " cancelado");
            return true;
        }
        return false;
    }
    
    // Método para ver el próximo pedido sin procesarlo
    public Pedido verProximoPedido() {
        return colaPedidos.peek();
    }
    
    // Método para obtener todos los pedidos en cola
    public ArrayList<Pedido> obtenerPedidosEnCola() {
        return new ArrayList<>(colaPedidos);
    }
    
    // Método para obtener el historial completo
    public ArrayList<Pedido> obtenerHistorialPedidos() {
        return new ArrayList<>(historialPedidos);
    }
    
    // Método para ver todos los pedidos en cola
    public void mostrarPedidosPendientes() {
        if (estaVacia()) {
            System.out.println("No hay pedidos pendientes");
            return;
        }
        
        System.out.println("=== PEDIDOS PENDIENTES EN COLA ===");
        int posicion = 1;
        for (Pedido pedido : colaPedidos) {
            System.out.println(posicion++ + ". " + pedido.toString());
        }
        System.out.println("==================================");
    }
    
    // Métodos de utilidad
    public boolean estaVacia() {
        return colaPedidos.isEmpty();
    }
    
    public int cantidadPedidosPendientes() {
        return colaPedidos.size();
    }
    
    public void limpiarCola() {
        colaPedidos.clear();
        historialPedidos.clear();
        siguienteId = 1;
        System.out.println("Cola de pedidos limpiada");
    }
    
    // Método para buscar pedido por ID
    public Pedido buscarPedidoPorId(int idPedido) {
        for (Pedido pedido : colaPedidos) {
            if (pedido.getIdPedido() == idPedido) {
                return pedido;
            }
        }
        
        // Buscar en historial si no está en cola activa
        for (Pedido pedido : historialPedidos) {
            if (pedido.getIdPedido() == idPedido) {
                return pedido;
            }
        }
        return null;
    }
}