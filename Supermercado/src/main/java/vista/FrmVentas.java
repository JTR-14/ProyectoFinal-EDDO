/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import modelo.*;
import utiles.*;
import datos.*;
import java.awt.Window;
import javax.swing.*;
import java.util.*;
import java.util.logging.Level;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Toledo
 */
public class FrmVentas extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmVentas.class.getName());
    private GestorSistema gestor;
    private Venta ventaActual;
    private DefaultTableModel modeloDetalles;
    private ArrayList<Cliente> listaClientes;
    private int idUsuarioActual;
    private Cliente clienteSeleccionado;
    
    public FrmVentas() {
        initComponents();
        gestor = GestorSistema.getInstancia();
        idUsuarioActual = gestor.getUsuarioActual().getIdUsuario();
        clienteSeleccionado = null;
        
        // IMPORTANTE: Primero cargar clientes, luego inicializar venta
        cargarClientes();
        inicializarVenta();
        inicializarTablaDetalles();
        actualizarNumeroVenta();
    }
    
    private void inicializarVenta() {
        ventaActual = new Venta();
        ventaActual.setIdUsuario(idUsuarioActual);
        actualizarTotales();
        clienteSeleccionado = null;
        
        // SOLO seleccionar índice 0 si hay elementos
        if (cmbCliente.getItemCount() > 0) {
            cmbCliente.setSelectedIndex(0);
        }
        
        txtCodigo.requestFocus();
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
            tblDetalles.getColumnModel().getColumn(0).setPreferredWidth(80);   // Código
            tblDetalles.getColumnModel().getColumn(1).setPreferredWidth(200);  // Producto
            tblDetalles.getColumnModel().getColumn(2).setPreferredWidth(60);   // Cantidad
            tblDetalles.getColumnModel().getColumn(3).setPreferredWidth(80);   // P. Unitario
            tblDetalles.getColumnModel().getColumn(4).setPreferredWidth(70);   // Stock Disp.
            tblDetalles.getColumnModel().getColumn(5).setPreferredWidth(80);   // Subtotal
        }
    }
    
    private void cargarClientes() {
        try {
            listaClientes = DALCliente.obtenerClientes();
            cmbCliente.removeAllItems();
            
            // AGREGAR ESTA LÍNEA PARA CLIENTE POR DEFECTO
            cmbCliente.addItem("-- Seleccione Cliente --");
            
            for (Cliente cliente : listaClientes) {
                cmbCliente.addItem(cliente.getNombre() + " - DNI: " + cliente.getDni());
            }
            
            // Si no hay clientes, mostrar mensaje
            if (listaClientes.isEmpty()) {
                logger.warning("No hay clientes registrados en la base de datos");
                cmbCliente.addItem("NO HAY CLIENTES REGISTRADOS");
                cmbCliente.setEnabled(false);
            }
            
            logger.log(Level.INFO, "Clientes cargados: {0}", listaClientes.size());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al cargar clientes: {0}", ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al cargar clientes: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            
            // Asegurar que el combo tenga al menos un elemento
            cmbCliente.addItem("-- Error al cargar clientes --");
            cmbCliente.setEnabled(false);
        }
    }
    
    private void actualizarNumeroVenta() {
        try {
            int siguienteNumero = DALVentas.obtenerSiguienteNumeroVenta();
            lblNumeroVenta.setText("Venta #: " + siguienteNumero);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error al obtener número de venta: {0}", ex.getMessage());
            lblNumeroVenta.setText("Venta #: --");
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
                
                // Verificar si ya está en la venta
                int indiceExistente = buscarProductoEnVenta(producto.getIdProducto());
                if (indiceExistente >= 0) {
                    // Aumentar cantidad del producto existente
                    DetalleVenta detalle = ventaActual.getDetalles().get(indiceExistente);
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
                    actualizarFilaTabla(indiceExistente, detalle, producto);
                } else {
                    // Agregar nuevo producto
                    DetalleVenta nuevoDetalle = new DetalleVenta(producto, 1);
                    ventaActual.agregarDetalle(nuevoDetalle);
                    agregarFilaTabla(nuevoDetalle, producto);
                }
                
                actualizarTotales();
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
            logger.log(Level.SEVERE, "Error al buscar producto: {0}", ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al buscar producto: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int buscarProductoEnVenta(int idProducto) {
        for (int i = 0; i < ventaActual.getDetalles().size(); i++) {
            if (ventaActual.getDetalles().get(i).getIdProducto() == idProducto) {
                return i;
            }
        }
        return -1;
    }
    
    private void agregarFilaTabla(DetalleVenta detalle, Producto producto) {
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
    
    private void actualizarFilaTabla(int indice, DetalleVenta detalle, Producto producto) {
        modeloDetalles.setValueAt(detalle.getCantidad(), indice, 2);
        modeloDetalles.setValueAt(String.format("S/ %.2f", detalle.getSubtotal()), indice, 5);
    }
    
    private void actualizarTotales() {
        double subtotal = ventaActual.getMontoTotal();
        double igv = subtotal * 0.18; // IGV 18%
        double total = subtotal + igv;
        
        txtSubtotal.setText(String.format("S/ %.2f", subtotal));
        txtIGV.setText(String.format("S/ %.2f", igv));
        txtTotal.setText(String.format("S/ %.2f", total));
        
        // Actualizar contador de productos
        int totalProductos = 0;
        if (ventaActual.getDetalles() != null) {
            for (DetalleVenta detalle : ventaActual.getDetalles()) {
                totalProductos += detalle.getCantidad();
            }
        }
        lblTotalProductos.setText("Productos: " + totalProductos + " items");
    }
    
    private void reiniciarVenta() {
        // Guardar cliente seleccionado si existe
        int clienteIndex = cmbCliente.getSelectedIndex();
        
        // Reiniciar todo
        ventaActual = new Venta();
        ventaActual.setIdUsuario(idUsuarioActual);
        modeloDetalles.setRowCount(0);
        
        // Restaurar cliente si había uno seleccionado y hay elementos
        if (clienteIndex > 0 && cmbCliente.getItemCount() > clienteIndex) {
            cmbCliente.setSelectedIndex(clienteIndex);
        } else if (cmbCliente.getItemCount() > 0) {
            cmbCliente.setSelectedIndex(0);
        }
        
        // Actualizar número de venta
        actualizarNumeroVenta();
        actualizarTotales();
        
        txtCodigo.requestFocus();
    }
    
    private void cancelarVenta() {
        if (!ventaActual.getDetalles().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Cancelar venta actual?\n\n" +
                "Se perderán todos los productos agregados (" + 
                ventaActual.getDetalles().size() + " productos).",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                reiniciarVenta();
                JOptionPane.showMessageDialog(this,
                    "Venta cancelada",
                    "Cancelación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "No hay productos en la venta para cancelar",
                "Venta Vacía",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
 
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        datos = new javax.swing.JPanel();
        txtCodigo = new javax.swing.JTextField();
        txtSubtotal = new javax.swing.JTextField();
        txtIGV = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        cmbCliente = new javax.swing.JComboBox<>();
        lblNumeroVenta = new javax.swing.JLabel();
        lblTotalProductos = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnRegistrar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        datos.setBackground(new java.awt.Color(243, 243, 243));
        datos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 14))); // NOI18N

        txtCodigo.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtCodigo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N
        txtCodigo.addActionListener(this::txtCodigoActionPerformed);

        txtSubtotal.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtSubtotal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SubTotal:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtIGV.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtIGV.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "IGV:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtTotal.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtTotal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        cmbCliente.setBorder(javax.swing.BorderFactory.createTitledBorder("Cliente"));

        lblNumeroVenta.setBackground(new java.awt.Color(153, 153, 153));
        lblNumeroVenta.setForeground(new java.awt.Color(153, 153, 153));
        lblNumeroVenta.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblTotalProductos.setForeground(java.awt.Color.darkGray);
        lblTotalProductos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout datosLayout = new javax.swing.GroupLayout(datos);
        datos.setLayout(datosLayout);
        datosLayout.setHorizontalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtCodigo)
                    .addComponent(txtSubtotal)
                    .addComponent(cmbCliente, 0, 224, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNumeroVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50))
        );
        datosLayout.setVerticalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addComponent(lblTotalProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblNumeroVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39))
        );

        jLabel1.setFont(new java.awt.Font("Montserrat ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("Ventas");

        btnRegistrar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnRegistrar.setText("REGISTRAR");
        btnRegistrar.addActionListener(this::btnRegistrarActionPerformed);

        btnEliminar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnEliminar.setText("ELIMINAR");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);

        btnCancelar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnCancelar.setText("CANCELAR");
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);

        btnVolver.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(this::btnVolverActionPerformed);

        btnBuscar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnBuscar.setText("BUSCAR");
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(btnRegistrar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addComponent(btnBuscar)
                .addGap(44, 44, 44)
                .addComponent(btnEliminar)
                .addGap(44, 44, 44)
                .addComponent(btnCancelar)
                .addGap(59, 59, 59)
                .addComponent(btnVolver)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegistrar)
                    .addComponent(btnCancelar)
                    .addComponent(btnVolver)
                    .addComponent(btnBuscar)
                    .addComponent(btnEliminar))
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(258, 258, 258))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(datos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 16, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 623, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(datos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarActionPerformed
        if (cmbCliente.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un cliente para la venta",
                "Cliente Requerido",
                JOptionPane.WARNING_MESSAGE);
            cmbCliente.requestFocus();
            return;
        }
        
        // Validar que haya productos
        if (ventaActual.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Agregue productos a la venta",
                "Venta Vacía",
                JOptionPane.WARNING_MESSAGE);
            txtCodigo.requestFocus();
            return;
        }
        
        // Validar stock (segunda validación por seguridad)
        if (!ventaActual.verificarStockDisponible()) {
            JOptionPane.showMessageDialog(this,
                "Stock insuficiente para uno o más productos.\n" +
                "Revise las cantidades solicitadas.",
                "Error de Stock",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Obtener cliente seleccionado
            int indiceCliente = cmbCliente.getSelectedIndex() - 1;
            clienteSeleccionado = listaClientes.get(indiceCliente);
            ventaActual.setIdCliente(clienteSeleccionado.getIdCliente());
            
            // Registrar venta en base de datos (esto actualiza el stock automáticamente)
            int idVenta = DALVentas.registrarVenta(ventaActual);
            
            if (idVenta > 0) {
                // Mostrar resumen de la venta
                StringBuilder resumen = new StringBuilder();
                resumen.append("╔══════════════════════════════════════╗\n");
                resumen.append("║        VENTA REGISTRADA EXITOSAMENTE ║\n");
                resumen.append("╚══════════════════════════════════════╝\n\n");
                resumen.append("N° Venta: ").append(idVenta).append("\n");
                resumen.append("Fecha: ").append(ventaActual.getFecha()).append("\n");
                resumen.append("Cliente: ").append(clienteSeleccionado.getNombre()).append("\n");
                resumen.append("DNI: ").append(clienteSeleccionado.getDni()).append("\n");
                resumen.append("──────────────────────────────────────\n");
                resumen.append("DETALLE DE PRODUCTOS:\n");
                
                for (int i = 0; i < ventaActual.getDetalles().size(); i++) {
                    DetalleVenta detalle = ventaActual.getDetalles().get(i);
                    resumen.append(String.format("%2d. %-20s x%2d = S/ %6.2f\n", 
                        i + 1, 
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getSubtotal()));
                }
                
                resumen.append("──────────────────────────────────────\n");
                resumen.append(String.format("Subtotal:   S/ %10.2f\n", ventaActual.getMontoTotal()));
                resumen.append(String.format("IGV (18%%):  S/ %10.2f\n", ventaActual.getMontoTotal() * 0.18));
                resumen.append(String.format("TOTAL:      S/ %10.2f\n", ventaActual.getMontoTotal() * 1.18));
                resumen.append("══════════════════════════════════════\n");
                resumen.append("¡Gracias por su compra!");
                
                // Mostrar comprobante
                JOptionPane.showMessageDialog(this,
                    resumen.toString(),
                    "Comprobante de Venta #" + idVenta,
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Registrar en historial de navegación
                gestor.getHistorialNavegacion().navegarA("NuevaVentaRegistrada");
                
                // Registrar en logger
                logger.info("Venta registrada exitosamente - ID: " + idVenta + 
                           " - Cliente ID: " + clienteSeleccionado.getIdCliente() + 
                           " - Total: S/" + ventaActual.getMontoTotal() + 
                           " - Productos: " + ventaActual.getDetalles().size());
                
                // Reiniciar formulario para nueva venta
                reiniciarVenta();
                
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al registrar la venta en la base de datos.\n" +
                    "Por favor, intente nuevamente.",
                    "Error en Registro",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            logger.severe("Error crítico al registrar venta: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error crítico: " + ex.getMessage() + "\n" +
                "La venta no se ha registrado. Contacte al administrador.",
                "Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRegistrarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        int fila = tblDetalles.getSelectedRow();
        if (fila >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar producto de la venta?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                ventaActual.eliminarDetalle(fila);
                modeloDetalles.removeRow(fila);
                actualizarTotales();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Seleccione un producto para eliminar",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        cancelarVenta();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        if (!ventaActual.getDetalles().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Hay productos en la venta actual.\n" +
                "¿Desea cancelar la venta y volver al menú principal?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Registrar navegación de regreso
        gestor.getHistorialNavegacion().navegarA("Principal");
        logger.info("Volviendo al formulario principal desde Ventas");
        
        // Cerrar este formulario
        this.dispose();
        
        // Buscar si ya hay una instancia de FrmPrincipal abierta
        // Si la encuentra, la trae al frente en lugar de crear una nueva
        java.awt.EventQueue.invokeLater(() -> {
            boolean principalEncontrado = false;
            Window[] windows = Window.getWindows();
            
            for (Window window : windows) {
                if (window instanceof FrmPrincipal && window.isVisible()) {
                    // Ya hay una instancia abierta, traerla al frente
                    window.setVisible(true);
                    window.toFront();
                    window.requestFocus();
                    principalEncontrado = true;
                    logger.info("FrmPrincipal encontrado y traído al frente desde Ventas");
                    break;
                }
            }
            
            // Si no se encontró ninguna instancia abierta de FrmPrincipal
            // y hay un usuario logueado, crear una nueva (solo en este caso)
            if (!principalEncontrado && gestor.getUsuarioActual() != null) {
                logger.info("Creando nueva instancia de FrmPrincipal desde Ventas (no se encontró una abierta)");
                new FrmPrincipal(gestor.getUsuarioActual()).setVisible(true);
            }
        });
    }//GEN-LAST:event_btnVolverActionPerformed

    private void tblDetallesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDetallesMouseClicked
        
    }//GEN-LAST:event_tblDetallesMouseClicked

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarProducto();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void txtCodigoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoActionPerformed
        buscarProducto();
    }//GEN-LAST:event_txtCodigoActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<String> cmbCliente;
    private javax.swing.JPanel datos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblNumeroVenta;
    private javax.swing.JLabel lblTotalProductos;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtIGV;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
