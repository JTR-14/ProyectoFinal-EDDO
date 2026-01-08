/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import datos.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.*;
import utiles.GestorSistema;

/**
 *
 * @author Toledo
 */
public class FrmPedidosOnline extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmPedidosOnline.class.getName());
    private GestorSistema gestor;
    private Pedido pedidoActual;
    private DefaultTableModel modeloDetalles;
    private DefaultTableModel modeloCola;
    private ArrayList<Cliente> listaClientes;
    
    public FrmPedidosOnline() {
        initComponents();
        gestor = GestorSistema.getInstancia();
        pedidoActual = new Pedido();
        cargarClientes();
        inicializarTablaDetalles();
        inicializarTablaCola();
        actualizarEstadoCola();
        configurarMetodosPago();
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
                return false; // Tabla no editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class;     // Cantidad
                if (columnIndex == 3 || columnIndex == 5) return Double.class; // Precio y Subtotal
                if (columnIndex == 4) return Integer.class;     // Stock
                return String.class;
            }
        };
        
        modeloDetalles.addColumn("Código");
        modeloDetalles.addColumn("Producto");
        modeloDetalles.addColumn("Cantidad");
        modeloDetalles.addColumn("P. Unitario");
        modeloDetalles.addColumn("Stock Disp.");
        modeloDetalles.addColumn("Subtotal");
        
        tblDetalles.setModel(modeloDetalles);
        
        // Ajustar ancho de columnas
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
                return false; // Tabla no editable
            }
        };
        
        modeloCola.addColumn("#");
        modeloCola.addColumn("ID Pedido");
        modeloCola.addColumn("Cliente");
        modeloCola.addColumn("Total");
        modeloCola.addColumn("Estado");
        modeloCola.addColumn("Productos");
        
        tblColaPedidos.setModel(modeloCola);
        
        // Ajustar ancho de columnas
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
            
            for (Cliente cliente : listaClientes) {
                cmbCliente.addItem(cliente.getNombre() + " - DNI: " + cliente.getDni());
            }
            
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
            Producto producto = DALProductos.buscarPorCodigo(codigo);
            if (producto != null) {
                // Verificar stock
                if (producto.getStockActual() <= 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Producto sin stock disponible", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    txtCodigo.setText("");
                    txtCodigo.requestFocus();
                    return;
                }
                
                // Verificar stock mínimo
                if (producto.getStockActual() <= producto.getStockMinimo()) {
                    JOptionPane.showMessageDialog(this, 
                        "ALERTA: Stock bajo (" + producto.getStockActual() + " unidades)\n" +
                        "Stock mínimo: " + producto.getStockMinimo(),
                        "Advertencia de Stock", 
                        JOptionPane.WARNING_MESSAGE);
                }
                
                // Verificar si ya está en el pedido
                int indiceExistente = buscarProductoEnPedido(producto.getIdProducto());
                if (indiceExistente >= 0) {
                    // Aumentar cantidad del producto existente
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
                    // Agregar nuevo producto
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
        double igv = subtotal * 0.18; // IGV 18%
        double total = subtotal + igv;
        
        txtSubtotal.setText(String.format("S/ %.2f", subtotal));
        txtIGV.setText(String.format("S/ %.2f", igv));
        txtTotal.setText(String.format("S/ %.2f", total));
        
        // Actualizar contador de productos
        int totalProductos = pedidoActual.getDetalles().stream()
                .mapToInt(DetallePedido::getCantidad)
                .sum();
        lblProductosPedido.setText("Productos en pedido: " + totalProductos + " items");
    }
    
    
    private void actualizarTablaCola() {
        modeloCola.setRowCount(0);
        modelo.Pedido proximo = gestor.getColaPedidos().verProximoPedido();
        if (proximo != null) {
            Object[] fila = {
                1,
                "PED-" + String.format("%03d", proximo.getIdPedido()),
                proximo.getCliente().getNombre(),
                String.format("S/ %.2f", proximo.calcularTotal()),
                proximo.getEstado(),
                proximo.getDetalles().size() + " items"
            };
            modeloCola.addRow(fila);
        }
        
        // Actualizar información de estado
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
        
        // Obtener próximo pedido
        modelo.Pedido pedidoProcesar = gestor.getColaPedidos().procesarSiguientePedido();
        
        if (pedidoProcesar != null) {
            // Mostrar detalles del pedido a procesar
            int opcion = JOptionPane.showConfirmDialog(this,
                "📦 PROCESAR PEDIDO #" + pedidoProcesar.getIdPedido() + "\n\n" +
                "Cliente: " + pedidoProcesar.getCliente().getNombre() + "\n" +
                "Total: " + String.format("S/ %.2f", pedidoProcesar.calcularTotal()) + "\n" +
                "Productos: " + pedidoProcesar.getDetalles().size() + " items\n\n" +
                "¿Desea marcar este pedido como EN PROCESO?",
                "Procesar Pedido",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (opcion == JOptionPane.YES_OPTION) {
                // Aquí iría la lógica para convertir el pedido en una venta real
                // y actualizar el stock en la base de datos
                
                // Por ahora, solo marcamos como procesado
                gestor.getColaPedidos().completarPedido(pedidoProcesar);
                
                JOptionPane.showMessageDialog(this,
                    "✅ Pedido #" + pedidoProcesar.getIdPedido() + " marcado como EN PROCESO\n\n" +
                    "El pedido ha sido retirado de la cola y está listo\n" +
                    "para ser preparado y enviado.",
                    "Pedido en Proceso",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Actualizar tabla
                actualizarTablaCola();
                actualizarEstadoCola();
                
                logger.info("Pedido procesado - ID: " + pedidoProcesar.getIdPedido());
            }
        }
    }
    
    private void verDetallesPedido() {
        if (gestor.getColaPedidos().estaVacia()) {
            JOptionPane.showMessageDialog(this,
                "No hay pedidos en la cola",
                "Cola Vacía",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        modelo.Pedido proximo = gestor.getColaPedidos().verProximoPedido();
        if (proximo != null) {
            JOptionPane.showMessageDialog(this,
                proximo.obtenerDetallesFormateados(),
                "Detalles del Próximo Pedido #" + proximo.getIdPedido(),
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void cancelarPedido() {
        if (gestor.getColaPedidos().estaVacia()) {
            JOptionPane.showMessageDialog(this,
                "No hay pedidos en la cola para cancelar",
                "Cola Vacía",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        modelo.Pedido proximo = gestor.getColaPedidos().verProximoPedido();
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
                
                // Actualizar tabla
                actualizarTablaCola();
                actualizarEstadoCola();
                
                logger.info("Pedido cancelado - ID: " + proximo.getIdPedido());
            }
        }
    }
    
    private void reiniciarPedido() {
        // Guardar cliente seleccionado si existe
        int clienteIndex = cmbCliente.getSelectedIndex();
        
        // Reiniciar pedido
        pedidoActual = new Pedido();
        modeloDetalles.setRowCount(0);
        
        // Restaurar cliente si había uno seleccionado
        if (clienteIndex > 0) {
            cmbCliente.setSelectedIndex(clienteIndex);
        }
        
        // Limpiar otros campos
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtNotas.setText("");
        cmbMetodoPago.setSelectedIndex(0);
        
        actualizarTotalesPedido();
        txtCodigo.requestFocus();
    }
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtId = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        datos = new javax.swing.JPanel();
        txtCodigo = new javax.swing.JTextField();
        txtDireccion = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        cmbCliente = new javax.swing.JComboBox<>();
        lblEstadoCola = new javax.swing.JLabel();
        lblProductosPedido = new javax.swing.JLabel();
        txtIGV = new javax.swing.JTextField();
        txtNotas = new javax.swing.JTextField();
        txtSubtotal = new javax.swing.JTextField();
        cmbMetodoPago = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnCancelarCola = new javax.swing.JButton();
        btnAgregarProducto = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnProcesar = new javax.swing.JButton();
        btnVerDetalles = new javax.swing.JButton();
        btnCancelarPedido = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblColaPedidos = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        datos.setBackground(new java.awt.Color(243, 243, 243));
        datos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 14))); // NOI18N

        txtCodigo.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtCodigo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtDireccion.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtDireccion.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Dirección:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtTelefono.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtTelefono.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Teléfono:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtTotal.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtTotal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        cmbCliente.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        cmbCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cliente:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        lblEstadoCola.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblProductosPedido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        txtIGV.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtIGV.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "IGV:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtNotas.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtNotas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Notas:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtSubtotal.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtSubtotal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Subtotal", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        cmbMetodoPago.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        cmbMetodoPago.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Método pago:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        javax.swing.GroupLayout datosLayout = new javax.swing.GroupLayout(datos);
        datos.setLayout(datosLayout);
        datosLayout.setHorizontalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(56, 56, 56)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNotas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIGV, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEstadoCola, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProductosPedido, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbMetodoPago, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
        );
        datosLayout.setVerticalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblEstadoCola, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(cmbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addComponent(cmbMetodoPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(datosLayout.createSequentialGroup()
                        .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNotas, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblProductosPedido, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))))
        );

        jLabel1.setFont(new java.awt.Font("Montserrat ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("Pedidos Online");

        btnGuardar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        btnCancelarCola.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnCancelarCola.setText("CANCELAR COLA");
        btnCancelarCola.addActionListener(this::btnCancelarColaActionPerformed);

        btnAgregarProducto.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnAgregarProducto.setText("AGREGAR PRODUCTO");
        btnAgregarProducto.addActionListener(this::btnAgregarProductoActionPerformed);

        btnBuscar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnBuscar.setText("BUSCAR");
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);

        btnVolver.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(this::btnVolverActionPerformed);

        btnEliminar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnEliminar.setText("ELIMINAR");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);

        btnProcesar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnProcesar.setText("PROCESAR");
        btnProcesar.addActionListener(this::btnProcesarActionPerformed);

        btnVerDetalles.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVerDetalles.setText("VER DETALLES");
        btnVerDetalles.addActionListener(this::btnVerDetallesActionPerformed);

        btnCancelarPedido.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnCancelarPedido.setText("CANCELAR PEDIDO");
        btnCancelarPedido.addActionListener(this::btnCancelarPedidoActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(btnGuardar)
                .addGap(11, 11, 11)
                .addComponent(btnBuscar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAgregarProducto)
                .addGap(18, 18, 18)
                .addComponent(btnEliminar)
                .addGap(18, 18, 18)
                .addComponent(btnProcesar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnVerDetalles)
                .addGap(18, 18, 18)
                .addComponent(btnCancelarCola)
                .addGap(34, 34, 34)
                .addComponent(btnCancelarPedido)
                .addGap(35, 35, 35)
                .addComponent(btnVolver)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnAgregarProducto)
                    .addComponent(btnVolver)
                    .addComponent(btnEliminar)
                    .addComponent(btnProcesar)
                    .addComponent(btnVerDetalles)
                    .addComponent(btnCancelarPedido)
                    .addComponent(btnBuscar)
                    .addComponent(btnCancelarCola))
                .addGap(21, 21, 21))
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

        tblColaPedidos.setBorder(javax.swing.BorderFactory.createTitledBorder("Cola de pedidos"));
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
        jScrollPane2.setViewportView(tblColaPedidos);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(289, 289, 289)
                .addComponent(jLabel1)
                .addGap(26, 26, 26))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 735, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(datos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 40, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(datos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarProducto();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        if (cmbCliente.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un cliente para el pedido",
                "Cliente Requerido",
                JOptionPane.WARNING_MESSAGE);
            cmbCliente.requestFocus();
            return;
        }
        
        // Validar dirección de entrega
        if (txtDireccion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingrese la dirección de entrega",
                "Dirección Requerida",
                JOptionPane.WARNING_MESSAGE);
            txtDireccion.requestFocus();
            return;
        }
        
        // Validar teléfono de contacto
        if (txtTelefono.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingrese el teléfono de contacto",
                "Teléfono Requerido",
                JOptionPane.WARNING_MESSAGE);
            txtTelefono.requestFocus();
            return;
        }
        
        // Validar que haya productos
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
            // Configurar datos del pedido
            int indiceCliente = cmbCliente.getSelectedIndex() - 1;
            Cliente cliente = listaClientes.get(indiceCliente);
            pedidoActual.setCliente(cliente);
            pedidoActual.setDireccionEntrega(txtDireccion.getText().trim());
            pedidoActual.setTelefonoContacto(txtTelefono.getText().trim());
            pedidoActual.setMetodoPago(cmbMetodoPago.getSelectedItem().toString());
            pedidoActual.setNotas(txtNotas.getText().trim());
            
            // Agregar pedido a la cola (COLA FIFO)
            if (gestor.getColaPedidos().agregarPedido(pedidoActual)) {
                // Mostrar confirmación
                String mensaje = "✅ PEDIDO REGISTRADO EN COLA EXITOSAMENTE\n\n" +
                               "Número de Pedido: #" + pedidoActual.getIdPedido() + "\n" +
                               "Cliente: " + cliente.getNombre() + "\n" +
                               "Dirección: " + pedidoActual.getDireccionEntrega() + "\n" +
                               "Total: " + txtTotal.getText() + "\n" +
                               "Posición en cola: " + gestor.getColaPedidos().cantidadPedidosPendientes() + "\n" +
                               "Estado: " + pedidoActual.getEstado() + "\n\n" +
                               "El pedido será procesado en orden de llegada (FIFO).";
                
                JOptionPane.showMessageDialog(this,
                    mensaje,
                    "Pedido Agregado a la Cola",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Registrar en logger
                logger.info("Pedido online agregado a cola - ID: " + pedidoActual.getIdPedido() + 
                           " - Cliente: " + cliente.getNombre() + 
                           " - Total: " + pedidoActual.calcularTotal() + 
                           " - Posición: " + gestor.getColaPedidos().cantidadPedidosPendientes());
                
                // Actualizar tabla de cola
                actualizarTablaCola();
                actualizarEstadoCola();
                
                // Reiniciar formulario para nuevo pedido
                reiniciarPedido();
                
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al agregar el pedido a la cola",
                    "Error en Registro",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            logger.severe("Error al registrar pedido online: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error crítico: " + ex.getMessage() + "\n" +
                "El pedido no se ha registrado. Contacte al administrador.",
                "Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarColaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarColaActionPerformed
        cancelarPedido();
    }//GEN-LAST:event_btnCancelarColaActionPerformed

    private void btnAgregarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarProductoActionPerformed
        buscarProducto();
    }//GEN-LAST:event_btnAgregarProductoActionPerformed

    private void tblDetallesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDetallesMouseClicked
        
    }//GEN-LAST:event_tblDetallesMouseClicked

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        if (!pedidoActual.getDetalles().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Hay productos en el pedido actual.\n" +
                "¿Desea cancelar el pedido y volver al menú principal?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Registrar navegación de regreso
        gestor.getHistorialNavegacion().navegarA("Principal");
        logger.info("Volviendo al formulario principal desde Pedidos Online");
        
        // Cerrar este formulario
        this.dispose();
        
        // Volver al principal si hay usuario logueado
        if (gestor.getUsuarioActual() != null) {
            java.awt.EventQueue.invokeLater(() -> {
                new FrmPrincipal(gestor.getUsuarioActual()).setVisible(true);
            });
        }
    }//GEN-LAST:event_btnVolverActionPerformed

    private void tblColaPedidosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblColaPedidosMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblColaPedidosMouseClicked

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

    private void btnVerDetallesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerDetallesActionPerformed
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
    }//GEN-LAST:event_btnVerDetallesActionPerformed

    private void btnCancelarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarPedidoActionPerformed
        cancelarPedido();
    }//GEN-LAST:event_btnCancelarPedidoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelarCola;
    private javax.swing.JButton btnCancelarPedido;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnProcesar;
    private javax.swing.JButton btnVerDetalles;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<String> cmbCliente;
    private javax.swing.JComboBox<String> cmbMetodoPago;
    private javax.swing.JPanel datos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
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
