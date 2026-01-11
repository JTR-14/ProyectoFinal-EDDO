/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import datos.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import logica.ControladorProductos;
import modelo.*;
import utiles.GestorSistema;
import utiles.ListaEnlazadaDoble;
import utiles.NodoEnDoble;


public class FrmPedidosOnline extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmPedidosOnline.class.getName());
    private GestorSistema gestor;
    private Pedido pedidoActual;
    private DefaultTableModel modeloDetalles;
    private DefaultTableModel modeloCola;
    private ArrayList<Cliente> listaClientes;
    private FrmPrincipal principal;
    
    public FrmPedidosOnline() {
        initComponents();
        gestor = GestorSistema.getInstancia();
        pedidoActual = new Pedido();
        cargarClientes();
        inicializarTablaDetalles();
        inicializarTablaCola();
        actualizarEstadoCola();
        configurarMetodosPago();
        actualizarTablaCola(); // Cargar pedidos existentes
    }
    
    private void configurarMetodosPago() {
        cmbMetodoPago.removeAllItems();
        cmbMetodoPago.addItem("EFECTIVO");
        cmbMetodoPago.addItem("TARJETA DE CRÉDITO");
        cmbMetodoPago.addItem("TARJETA DE DÉBITO");
        cmbMetodoPago.addItem("TRANSFERENCIA BANCARIA");
        cmbMetodoPago.addItem("YAPE / PLIN");
    }
    
    private void inicializarTablaDetalles() {
        modeloDetalles = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        modeloDetalles.addColumn("Código");
        modeloDetalles.addColumn("Producto");
        modeloDetalles.addColumn("Cantidad");
        modeloDetalles.addColumn("P. Unitario");
        modeloDetalles.addColumn("Stock Disp.");
        modeloDetalles.addColumn("Subtotal");
        
        tblDetalles.setModel(modeloDetalles);
        
        // Ajustar anchos de columnas
        if (tblDetalles.getColumnModel().getColumnCount() > 0) {
            tblDetalles.getColumnModel().getColumn(0).setPreferredWidth(80);
            tblDetalles.getColumnModel().getColumn(1).setPreferredWidth(200);
            tblDetalles.getColumnModel().getColumn(2).setPreferredWidth(60);
            tblDetalles.getColumnModel().getColumn(3).setPreferredWidth(80);
            tblDetalles.getColumnModel().getColumn(4).setPreferredWidth(70);
            tblDetalles.getColumnModel().getColumn(5).setPreferredWidth(80);
        }
    }
    
    private void inicializarTablaCola() {
        modeloCola = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        modeloCola.addColumn("#");
        modeloCola.addColumn("ID Pedido");
        modeloCola.addColumn("Cliente");
        modeloCola.addColumn("Total");
        modeloCola.addColumn("Estado");
        modeloCola.addColumn("Productos");
        
        tblColaPedidos.setModel(modeloCola);
        
        // Ajustar anchos de columnas
        if (tblColaPedidos.getColumnModel().getColumnCount() > 0) {
            tblColaPedidos.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblColaPedidos.getColumnModel().getColumn(1).setPreferredWidth(80);
            tblColaPedidos.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblColaPedidos.getColumnModel().getColumn(3).setPreferredWidth(80);
            tblColaPedidos.getColumnModel().getColumn(4).setPreferredWidth(100);
            tblColaPedidos.getColumnModel().getColumn(5).setPreferredWidth(80);
        }
    }
    
    private void cargarClientes() {
        try {
            listaClientes = DALCliente.obtenerClientes();
            cmbCliente.removeAllItems();
            
            cmbCliente.addItem("-- Seleccione Cliente --");
           
            ArrayList<Cliente> clientesFiltrados = new ArrayList<>();
            
            for (Cliente cliente : listaClientes) {
                if (cliente.getIdCliente() != 1) { 
                    clientesFiltrados.add(cliente);
                    cmbCliente.addItem(cliente.getNombre());
                }
            }
            
            listaClientes = clientesFiltrados;
            
            logger.info("Clientes cargados para pedidos online: " + listaClientes.size());
            
        } catch (Exception ex) {
            logger.severe("Error al cargar clientes: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al cargar clientes: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buscarProducto() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Ingrese código de producto", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            txtCodigo.requestFocus();
            return;
        }
        
        try {
            Producto producto = ControladorProductos.buscarPorCodigo(codigo);
            if (producto != null) {
                if (producto.getStockActual() <= 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Producto sin stock disponible", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    txtCodigo.setText("");
                    txtCodigo.requestFocus();
                    return;
                }
                
                if (producto.getStockActual() <= producto.getStockMinimo()) {
                    JOptionPane.showMessageDialog(this, 
                        "ALERTA: Stock bajo (" + producto.getStockActual() + " unidades)\n" +
                        "Stock mínimo: " + producto.getStockMinimo(),
                        "Advertencia de Stock", 
                        JOptionPane.WARNING_MESSAGE);
                }
                
                int indiceExistente = buscarProductoEnPedido(producto.getIdProducto());
                if (indiceExistente >= 0) {
                    DetallePedido detalle = pedidoActual.getDetalles().get(indiceExistente);
                    int nuevaCantidad = detalle.getCantidad() + 1;
                    
                    if (nuevaCantidad > producto.getStockActual()) {
                        JOptionPane.showMessageDialog(this, 
                            "Stock insuficiente.\nDisponible: " + producto.getStockActual() + 
                            " unidades", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    detalle.setCantidad(nuevaCantidad);
                    actualizarFilaTablaDetalles(indiceExistente, detalle, producto);
                } else {
                    DetallePedido nuevoDetalle = new DetallePedido(producto, 1);
                    pedidoActual.agregarProducto(producto, 1);
                    agregarFilaTablaDetalles(nuevoDetalle, producto);
                }
                
                actualizarTotalesPedido();
                txtCodigo.setText("");
                txtCodigo.requestFocus();
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Producto no encontrado\nCódigo: " + codigo, 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtCodigo.setText("");
                txtCodigo.requestFocus();
            }
        } catch (Exception ex) {
            logger.severe("Error al buscar producto: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al buscar producto: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int buscarProductoEnPedido(int idProducto) {
        for (int i = 0; i < pedidoActual.getDetalles().size(); i++) {
            if (pedidoActual.getDetalles().get(i).getProducto().getIdProducto() == idProducto) {
                return i;
            }
        }
        return -1;
    }
    
    private void agregarFilaTablaDetalles(DetallePedido detalle, Producto producto) {
        Object[] fila = {
            producto.getCodigo(),
            producto.getNombre(),
            detalle.getCantidad(),
            String.format("S/ %.2f", producto.getPrecioVenta()),
            producto.getStockActual(),
            String.format("S/ %.2f", detalle.getSubtotal())
        };
        modeloDetalles.addRow(fila);
    }
    
    private void actualizarFilaTablaDetalles(int indice, DetallePedido detalle, Producto producto) {
        modeloDetalles.setValueAt(detalle.getCantidad(), indice, 2);
        modeloDetalles.setValueAt(String.format("S/ %.2f", detalle.getSubtotal()), indice, 5);
    }
    
    private void actualizarTotalesPedido() {
        double subtotal = pedidoActual.calcularTotal();
        double igv = subtotal * 0.18;
        double total = subtotal + igv;
        
        txtSubtotal.setText(String.format("S/ %.2f", subtotal));
        txtIGV.setText(String.format("S/ %.2f", igv));
        txtTotal.setText(String.format("S/ %.2f", total));
        
        int totalProductos = pedidoActual.getDetalles().stream()
                .mapToInt(DetallePedido::getCantidad)
                .sum();
        lblProductosPedido.setText("Productos en pedido: " + totalProductos + " items");
    }
    
    private void actualizarTablaCola() {
        modeloCola.setRowCount(0);
        
        ArrayList<Pedido> pedidosEnCola = gestor.getColaPedidos().obtenerPedidosEnCola();
        int posicion = 1;
        
        for (Pedido pedido : pedidosEnCola) {
            Object[] fila = {
                posicion++,
                "PED-" + String.format("%03d", pedido.getIdPedido()),
                pedido.getCliente() != null ? pedido.getCliente().getNombre() : "N/A",
                String.format("S/ %.2f", pedido.calcularTotal()),
                pedido.getEstado(),
                pedido.getDetalles().size() + " items"
            };
            modeloCola.addRow(fila);
        }
        
        actualizarEstadoCola();
    }
    
    private void actualizarEstadoCola() {
        int totalPedidos = gestor.getColaPedidos().cantidadPedidosPendientes();
        lblEstadoCola.setText("Pedidos en cola: " + totalPedidos);
        
        if (totalPedidos == 0) {
            lblEstadoCola.setForeground(new java.awt.Color(102, 102, 102));
        } else if (totalPedidos <= 3) {
            lblEstadoCola.setForeground(new java.awt.Color(0, 102, 0));
        } else if (totalPedidos <= 6) {
            lblEstadoCola.setForeground(new java.awt.Color(255, 153, 0));
        } else {
            lblEstadoCola.setForeground(new java.awt.Color(204, 0, 0));
        }
    }
    
    private void procesarSiguientePedido() {
    if (gestor.getColaPedidos().estaVacia()) {
        JOptionPane.showMessageDialog(this,
            "No hay pedidos pendientes en la cola",
            "Cola Vacía",
            JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    Pedido pedidoProcesar = gestor.getColaPedidos().procesarSiguientePedido();
    
    if (pedidoProcesar != null) {
        int opcion = JOptionPane.showConfirmDialog(this,
            "📦 PROCESAR PEDIDO #" + pedidoProcesar.getIdPedido() + "\n\n" +
            "Cliente: " + pedidoProcesar.getCliente().getNombre() + "\n" +
            "Total: " + String.format("S/ %.2f", pedidoProcesar.calcularTotal()) + "\n" +
            "Productos: " + pedidoProcesar.getDetalles().size() + " items\n\n" +
            "¿Desea convertir este pedido en una venta?",
            "Procesar Pedido",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (opcion == JOptionPane.YES_OPTION) {
            try {
                // Convertir Pedido a Venta
                Venta venta = convertirPedidoAVenta(pedidoProcesar);
                
                // REGISTRAR VENTA EN BD
                int idVenta = DALVentas.registrarVenta(venta);
                
                if (idVenta > 0) {
                    // ✅✅✅ CORRECCIÓN CRÍTICA: ACTUALIZAR STOCK EN ÁRBOL AVL
                    actualizarStockDesdeVenta(venta);
                    
                    // Marcar pedido como completado
                    gestor.getColaPedidos().completarPedido(pedidoProcesar);
                    
                    JOptionPane.showMessageDialog(this,
                        "✅ PEDIDO PROCESADO EXITOSAMENTE\n\n" +
                        "Pedido #" + pedidoProcesar.getIdPedido() + "\n" +
                        "Convertido a Venta #" + idVenta + "\n" +
                        "Cliente: " + pedidoProcesar.getCliente().getNombre() + "\n" +
                        "Total: " + String.format("S/ %.2f", venta.getMontoTotal()),
                        "Pedido Completado",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Forzar recarga del árbol AVL para sincronizar con BD
                    ControladorProductos.forzarRecarga();
                    
                    actualizarTablaCola();
                    actualizarEstadoCola();
                    
                    logger.info("Pedido procesado y convertido a venta - ID Pedido: " + 
                              pedidoProcesar.getIdPedido() + " - ID Venta: " + idVenta);
                    
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error al convertir pedido a venta",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    // Devolver pedido a la cola
                    pedidoProcesar.setEstado("PENDIENTE");
                    gestor.getColaPedidos().getColaPedidos().add(pedidoProcesar);
                }
                
            } catch (Exception ex) {
                logger.severe("Error al procesar pedido: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error al procesar pedido: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                // Devolver pedido a la cola
                pedidoProcesar.setEstado("PENDIENTE");
                gestor.getColaPedidos().getColaPedidos().add(pedidoProcesar);
            }
        } else {
            // Si no se confirma, devolver a la cola
            pedidoProcesar.setEstado("PENDIENTE");
            gestor.getColaPedidos().getColaPedidos().add(pedidoProcesar);
            actualizarTablaCola();
        }
    }
}

// ✅ NUEVO MÉTODO: Actualizar stock en árbol AVL desde venta
    private void actualizarStockDesdeVenta(Venta venta) {
        try {
            NodoEnDoble<DetalleVenta> p = venta.getDetalles().getPrimero();
            
            while (p != null) {
                DetalleVenta detalle = p.getInfo();
                
                // Buscar producto en el árbol AVL
                Producto producto = ControladorProductos.buscarPorCodigo(detalle.getProducto().getCodigo());
                
                if (producto != null) {
                    // Calcular nuevo stock
                    int nuevoStock = producto.getStockActual() - detalle.getCantidad();
                    if (nuevoStock < 0) nuevoStock = 0;
                    
                    // Actualizar producto en árbol AVL
                    producto.setStockActual(nuevoStock);
                    ControladorProductos.actualizarProductoEnArbol(producto);
                    
                    logger.info("Stock actualizado - Producto: " + producto.getCodigo() + 
                              " - Cantidad vendida: " + detalle.getCantidad() + 
                              " - Stock nuevo: " + nuevoStock);
                } else {
                    logger.warning("Producto no encontrado en árbol AVL: " + 
                                 detalle.getProducto().getCodigo());
                }
                
                p = p.getSgte();
            }
            
        } catch (Exception ex) {
            logger.severe("Error al actualizar stock desde venta: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
        
    // MÉTODO CORREGIDO: Convertir Pedido a Venta
    private Venta convertirPedidoAVenta(Pedido pedido) {
        Venta venta = new Venta();
        
        // Configurar datos básicos
        venta.setIdUsuario(gestor.getUsuarioActual().getIdUsuario());
        venta.setIdCliente(pedido.getCliente().getIdCliente());
        venta.setNombreCliente(pedido.getCliente().getNombre());
        
        // Usar fecha actual
        String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        venta.setFecha(fechaActual);
        
        // Crear lista de detalles de venta
        ListaEnlazadaDoble<DetalleVenta> detallesVenta = new ListaEnlazadaDoble<>();
        
        for (DetallePedido detallePedido : pedido.getDetalles()) {
            // Crear detalle de venta
            DetalleVenta detalleVenta = new DetalleVenta(
                detallePedido.getProducto(),
                detallePedido.getCantidad()
            );
            
            // Asegurar que el subtotal esté calculado
            detalleVenta.calcularSubTotal();
            
            detallesVenta.insertaAlFinal(detalleVenta);
        }
        
        // Asignar detalles y calcular totales
        venta.setDetalles(detallesVenta);
        venta.calcularMontoTotal(); // Calcula subtotal, IGV y total
        
        return venta;
    }
    
    private void verDetallesPedido() {
        if (gestor.getColaPedidos().estaVacia()) {
            JOptionPane.showMessageDialog(this,
                "No hay pedidos en la cola",
                "Cola Vacía",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Pedido proximo = gestor.getColaPedidos().verProximoPedido();
        if (proximo != null) {
            JOptionPane.showMessageDialog(this,
                generarDetallesFormateados(proximo),
                "Detalles del Próximo Pedido #" + proximo.getIdPedido(),
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Método auxiliar para formatear detalles
    private String generarDetallesFormateados(Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("           DETALLE DEL PEDIDO         \n");
        sb.append("══════════════════════════════════════\n");
        sb.append("Pedido #: ").append(pedido.getIdPedido()).append("\n");
        sb.append("Fecha: ").append(pedido.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("Estado: ").append(pedido.getEstado()).append("\n");
        sb.append("Cliente: ").append(pedido.getCliente().getNombre()).append("\n");
        sb.append("DNI: ").append(pedido.getCliente().getDni()).append("\n");
        sb.append("Teléfono: ").append(pedido.getTelefonoContacto()).append("\n");
        sb.append("Dirección: ").append(pedido.getDireccionEntrega()).append("\n");
        sb.append("Método Pago: ").append(pedido.getMetodoPago()).append("\n");
        sb.append("Notas: ").append(pedido.getNotas() != null ? pedido.getNotas() : "Ninguna").append("\n");
        sb.append("──────────────────────────────────────\n");
        sb.append("               PRODUCTOS              \n");
        sb.append("──────────────────────────────────────\n");
        
        int i = 1;
        for (DetallePedido detalle : pedido.getDetalles()) {
            sb.append(String.format("%2d. %-20s x%3d = S/ %7.2f\n", 
                i++,
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getSubtotal()));
        }
        
        sb.append("──────────────────────────────────────\n");
        double total = pedido.calcularTotal();
        double igv = total * 0.18;
        double totalConIGV = total + igv;
        sb.append(String.format("Subtotal:         S/ %10.2f\n", total));
        sb.append(String.format("IGV (18%%):        S/ %10.2f\n", igv));
        sb.append(String.format("TOTAL A PAGAR:    S/ %10.2f\n", totalConIGV));
        sb.append("══════════════════════════════════════\n");
        
        return sb.toString();
    }
    
    private void cancelarPedidoCola() {
        if (gestor.getColaPedidos().estaVacia()) {
            JOptionPane.showMessageDialog(this,
                "No hay pedidos en la cola para cancelar",
                "Cola Vacía",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Pedido proximo = gestor.getColaPedidos().verProximoPedido();
        if (proximo != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Cancelar el pedido #" + proximo.getIdPedido() + "?\n\n" +
                "Cliente: " + proximo.getCliente().getNombre() + "\n" +
                "Total: " + String.format("S/ %.2f", proximo.calcularTotal()) + "\n\n" +
                "Esta acción no se puede deshacer.",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                gestor.getColaPedidos().cancelarPedido(proximo);
                
                JOptionPane.showMessageDialog(this,
                    "❌ Pedido #" + proximo.getIdPedido() + " cancelado exitosamente",
                    "Pedido Cancelado",
                    JOptionPane.INFORMATION_MESSAGE);
                
                actualizarTablaCola();
                actualizarEstadoCola();
                
                logger.info("Pedido cancelado - ID: " + proximo.getIdPedido());
            }
        }
    }
    
    private void reiniciarPedido() {
        pedidoActual = new Pedido();
        modeloDetalles.setRowCount(0);
        
        // Restaurar valores por defecto
        if (cmbCliente.getItemCount() > 0) {
            cmbCliente.setSelectedIndex(0);
        }
        
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtNotas.setText("");
        cmbMetodoPago.setSelectedIndex(0);
        
        actualizarTotalesPedido();
        txtCodigo.requestFocus();
    }
    
    public void setPrincipal(FrmPrincipal principal) {
        this.principal = principal;
    }
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtId = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        datos = new javax.swing.JPanel();
        txtCodigo = new javax.swing.JTextField();
        lblEstadoCola = new javax.swing.JLabel();
        lblProductosPedido = new javax.swing.JLabel();
        btnAgregarProducto = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnProcesar = new javax.swing.JButton();
        btnCancelarPedido = new javax.swing.JButton();
        btnCancelarCola = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        txtSubtotal = new javax.swing.JTextField();
        txtIGV = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        cmbCliente = new javax.swing.JComboBox<>();
        txtTelefono = new javax.swing.JTextField();
        txtDireccion = new javax.swing.JTextField();
        cmbMetodoPago = new javax.swing.JComboBox<>();
        txtNotas = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblColaPedidos = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        datos.setBackground(new java.awt.Color(243, 243, 243));
        datos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Productos:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 14))); // NOI18N

        txtCodigo.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtCodigo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        lblEstadoCola.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblProductosPedido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnAgregarProducto.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnAgregarProducto.setText("AGREGAR PRODUCTO");
        btnAgregarProducto.addActionListener(this::btnAgregarProductoActionPerformed);

        javax.swing.GroupLayout datosLayout = new javax.swing.GroupLayout(datos);
        datos.setLayout(datosLayout);
        datosLayout.setHorizontalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(btnAgregarProducto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblEstadoCola, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(102, 102, 102)
                .addComponent(lblProductosPedido, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(125, 125, 125))
        );
        datosLayout.setVerticalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, Short.MAX_VALUE)
                        .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblProductosPedido, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEstadoCola, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(2, 2, 2))
                    .addComponent(btnAgregarProducto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(138, 138, 138))
        );

        jLabel1.setFont(new java.awt.Font("Montserrat ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("Pedidos");

        btnGuardar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnGuardar.setText("GUARDAR COMPRA");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        btnVolver.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(this::btnVolverActionPerformed);

        btnEliminar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnEliminar.setText("ELIMINAR PRODUCTO");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);

        btnProcesar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnProcesar.setText("PROCESAR SIGUIENTE PEDIDO");
        btnProcesar.addActionListener(this::btnProcesarActionPerformed);

        btnCancelarPedido.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnCancelarPedido.setText("CANCELAR PEDIDO");
        btnCancelarPedido.addActionListener(this::btnCancelarPedidoActionPerformed);

        btnCancelarCola.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnCancelarCola.setText("CANCELAR COLA");
        btnCancelarCola.addActionListener(this::btnCancelarColaActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(btnGuardar)
                .addGap(47, 47, 47)
                .addComponent(btnEliminar)
                .addGap(26, 26, 26)
                .addComponent(btnProcesar)
                .addGap(32, 32, 32)
                .addComponent(btnCancelarPedido)
                .addGap(18, 18, 18)
                .addComponent(btnCancelarCola)
                .addGap(34, 34, 34)
                .addComponent(btnVolver)
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnEliminar)
                    .addComponent(btnProcesar)
                    .addComponent(btnCancelarPedido)
                    .addComponent(btnVolver)
                    .addComponent(btnCancelarCola))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        tblDetalles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblDetalles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDetallesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblDetalles);

        txtSubtotal.setEditable(false);
        txtSubtotal.setBackground(new java.awt.Color(255, 255, 255));
        txtSubtotal.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtSubtotal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Subtotal", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtIGV.setEditable(false);
        txtIGV.setBackground(new java.awt.Color(255, 255, 255));
        txtIGV.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtIGV.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "IGV:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtTotal.setEditable(false);
        txtTotal.setBackground(new java.awt.Color(255, 255, 255));
        txtTotal.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtTotal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos del Cliente:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        cmbCliente.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        cmbCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cliente:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N
        cmbCliente.addActionListener(this::cmbClienteActionPerformed);

        txtTelefono.setEditable(false);
        txtTelefono.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtTelefono.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Teléfono:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtDireccion.setEditable(false);
        txtDireccion.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtDireccion.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Dirección:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        cmbMetodoPago.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        cmbMetodoPago.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Método pago:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtNotas.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtNotas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Notas:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addComponent(cmbMetodoPago, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                .addComponent(txtNotas, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbMetodoPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNotas, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        tblColaPedidos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblColaPedidos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblColaPedidosMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblColaPedidos);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(658, 658, 658)
                        .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 31, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(datos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 681, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 553, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(570, 570, 570))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(datos, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        if (cmbCliente.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un cliente para el pedido",
                "Cliente Requerido",
                JOptionPane.WARNING_MESSAGE);
            cmbCliente.requestFocus();
            return;
        }
        
        // Validar dirección
        if (txtDireccion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingrese la dirección de entrega",
                "Dirección Requerida",
                JOptionPane.WARNING_MESSAGE);
            txtDireccion.requestFocus();
            return;
        }
        
        // Validar teléfono
        if (txtTelefono.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingrese el teléfono de contacto",
                "Teléfono Requerido",
                JOptionPane.WARNING_MESSAGE);
            txtTelefono.requestFocus();
            return;
        }
        
        // Validar productos
        if (pedidoActual.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Agregue productos al pedido",
                "Pedido Vacío",
                JOptionPane.WARNING_MESSAGE);
            txtCodigo.requestFocus();
            return;
        }
        
        // Validar stock
        if (!pedidoActual.verificarStockDisponible()) {
            JOptionPane.showMessageDialog(this,
                "Stock insuficiente para uno o más productos.\n" +
                "Revise las cantidades solicitadas.",
                "Error de Stock",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Obtener cliente seleccionado
            int indiceCombo = cmbCliente.getSelectedIndex(); // 0 = placeholder, 1 = primer cliente real
            int indiceLista = indiceCombo - 1; // Convertir a índice de lista
            
            if (indiceLista < 0 || indiceLista >= listaClientes.size()) {
                JOptionPane.showMessageDialog(this,
                    "Error: Índice de cliente inválido",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Cliente cliente = listaClientes.get(indiceLista);
            
            // Configurar pedido
            pedidoActual.setCliente(cliente);
            pedidoActual.setDireccionEntrega(txtDireccion.getText().trim());
            pedidoActual.setTelefonoContacto(txtTelefono.getText().trim());
            pedidoActual.setMetodoPago(cmbMetodoPago.getSelectedItem().toString());
            pedidoActual.setNotas(txtNotas.getText().trim());
            
            // Guardar pedido en BD (necesitarías implementar DALPedidos)
            // Por ahora solo lo agregamos a la cola en memoria
            
            if (gestor.getColaPedidos().agregarPedido(pedidoActual)) {
                String mensaje = "✅ PEDIDO REGISTRADO EN COLA EXITOSAMENTE\n\n" +
                               "Número de Pedido: #" + pedidoActual.getIdPedido() + "\n" +
                               "Cliente: " + cliente.getNombre() + "\n" +
                               "Dirección: " + pedidoActual.getDireccionEntrega() + "\n" +
                               "Total: " + txtTotal.getText() + "\n" +
                               "Posición en cola: " + gestor.getColaPedidos().cantidadPedidosPendientes() + "\n" +
                               "Estado: " + pedidoActual.getEstado() + "\n\n" +
                               "El pedido será procesado en orden de llegada.";
                
                JOptionPane.showMessageDialog(this,
                    mensaje,
                    "Pedido Agregado a la Cola",
                    JOptionPane.INFORMATION_MESSAGE);
                
                logger.info("Pedido online agregado a cola - ID: " + pedidoActual.getIdPedido() + 
                           " - Cliente: " + cliente.getNombre() + 
                           " - Total: " + pedidoActual.calcularTotal() + 
                           " - Posición: " + gestor.getColaPedidos().cantidadPedidosPendientes());
                
                // Actualizar interfaz
                actualizarTablaCola();
                actualizarEstadoCola();
                reiniciarPedido();
                
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al agregar el pedido a la cola",
                    "Error en Registro",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            logger.severe("Error al registrar pedido online: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error crítico: " + ex.getMessage() + "\n" +
                "El pedido no se ha registrado. Contacte al administrador.",
                "Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarColaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarColaActionPerformed
        cancelarPedidoCola();
    }//GEN-LAST:event_btnCancelarColaActionPerformed

    private void btnAgregarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarProductoActionPerformed
        buscarProducto();
    }//GEN-LAST:event_btnAgregarProductoActionPerformed

    private void tblDetallesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDetallesMouseClicked
        
    }//GEN-LAST:event_tblDetallesMouseClicked
    
    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
       // 1. Validación de seguridad (Mantenemos tu lógica original)
        if (!pedidoActual.getDetalles().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Hay productos en el pedido actual.\n" +
                "¿Desea cancelar el pedido y volver al menú principal?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return; // Se queda aquí si dice que NO
            }
        }
        
        // 2. Logs y Gestor
        gestor.getHistorialNavegacion().navegarA("Principal");
        logger.info("Volviendo al formulario principal desde Pedidos Online");
        
        // 3. Regresar al Principal usando la referencia directa
        if (principal != null) {
            principal.setVisible(true);
            principal.toFront();
        }
        
        // 4. Ocultar esta ventana (No usar dispose para poder reciclarla)
        this.setVisible(false);
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        int fila = tblDetalles.getSelectedRow();
        if (fila >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar producto del pedido?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                pedidoActual.getDetalles().remove(fila);
                modeloDetalles.removeRow(fila);
                actualizarTotalesPedido();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Seleccione un producto para eliminar",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnProcesarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcesarActionPerformed
        procesarSiguientePedido();
    }//GEN-LAST:event_btnProcesarActionPerformed

    private void btnCancelarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarPedidoActionPerformed
        if (!pedidoActual.getDetalles().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Cancelar pedido actual?\n\n" +
                "Se perderán todos los productos agregados (" + 
                pedidoActual.getDetalles().size() + " productos).",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                reiniciarPedido();
                JOptionPane.showMessageDialog(this,
                    "Pedido actual cancelado",
                    "Cancelación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "No hay productos en el pedido para cancelar",
                "Pedido Vacío",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnCancelarPedidoActionPerformed

    private void cmbClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbClienteActionPerformed
        int indiceSeleccionado = cmbCliente.getSelectedIndex();

    if (indiceSeleccionado > 0 && listaClientes != null && !listaClientes.isEmpty()) {
        try {
            Cliente clienteElegido = listaClientes.get(indiceSeleccionado - 1);
            txtDireccion.setText(clienteElegido.getDireccion());
            txtTelefono.setText(clienteElegido.getTelefono());
        } catch (IndexOutOfBoundsException e) {
            txtDireccion.setText("");
            txtTelefono.setText("");
        }
    } else {
        txtDireccion.setText("");
        txtTelefono.setText("");
    }
    }//GEN-LAST:event_cmbClienteActionPerformed

    private void tblColaPedidosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblColaPedidosMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblColaPedidosMouseClicked

// Evento para detectar cambio de selección en el Combo

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JButton btnCancelarCola;
    private javax.swing.JButton btnCancelarPedido;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnProcesar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<String> cmbCliente;
    private javax.swing.JComboBox<String> cmbMetodoPago;
    private javax.swing.JPanel datos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblEstadoCola;
    private javax.swing.JLabel lblProductosPedido;
    private javax.swing.JTable tblColaPedidos;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtIGV;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNotas;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTelefono;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables

}
