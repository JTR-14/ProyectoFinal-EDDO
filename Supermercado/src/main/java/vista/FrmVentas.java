/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import datos.DALCliente;
import datos.DALVentas;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.DetalleVenta;
import modelo.Producto;
import modelo.Venta;
import utiles.GestorSistema;
import utiles.NodoEnDoble;
import logica.ControladorProductos;
/**
 *
 * @author tempano
 */
public class FrmVentas extends javax.swing.JFrame {
 private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmVentas.class.getName());
    private GestorSistema gestor;
    private Venta ventaActual;
    private DefaultTableModel modeloDetalles;
    private ArrayList<Cliente> listaClientes;
    private int idUsuarioActual;
    private Cliente clienteSeleccionado;
    private FrmPrincipal principal;

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
        if (cmbCliente1.getItemCount() > 0) {
            cmbCliente1.setSelectedIndex(0);
        }

        txtCodigo1.requestFocus();
    }

    public void setPrincipal(FrmPrincipal principal) {
        this.principal = principal;
    }

    private void inicializarTablaDetalles() {
        modeloDetalles = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }

        };

        modeloDetalles.addColumn("Código");
        modeloDetalles.addColumn("Producto");
        modeloDetalles.addColumn("Cantidad");
        modeloDetalles.addColumn("P. Unitario");
        modeloDetalles.addColumn("Stock Disp.");
        modeloDetalles.addColumn("Subtotal");

        tblDetalles.setModel(modeloDetalles);

    }

    private void cargarClientes() {
        try {
            listaClientes = DALCliente.obtenerClientes();
            cmbCliente1.removeAllItems();

            // AGREGAR ESTA LÍNEA PARA CLIENTE POR DEFECTO
            cmbCliente1.addItem("-- Seleccione Cliente --");

            for (Cliente cliente : listaClientes) {
                cmbCliente1.addItem(cliente.getNombre() + " - DNI: " + cliente.getDni());
            }

            // Si no hay clientes, mostrar mensaje
            if (listaClientes.isEmpty()) {
                logger.warning("No hay clientes registrados en la base de datos");
                cmbCliente1.addItem("NO HAY CLIENTES REGISTRADOS");
                cmbCliente1.setEnabled(false);
            }

            logger.log(Level.INFO, "Clientes cargados: {0}", listaClientes.size());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al cargar clientes: {0}", ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al cargar clientes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            // Asegurar que el combo tenga al menos un elemento
            cmbCliente1.addItem("-- Error al cargar clientes --");
            cmbCliente1.setEnabled(false);
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
        int numero = (int) spnCantidad1.getValue();
        String codigo = txtCodigo1.getText().trim();
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese código de producto",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            txtCodigo1.requestFocus();
            return;
        }

        try {
            Producto producto = ControladorProductos.buscarPorCodigo(codigo);
            if (producto != null) {
                // Verificar stock
                if (producto.getStockActual() <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Producto sin stock disponible",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    txtCodigo1.setText("");
                    txtCodigo1.requestFocus();
                    return;
                }

                // Verificar stock mínimo
                if (producto.getStockActual() <= producto.getStockMinimo()) {
                    JOptionPane.showMessageDialog(this,
                            "ALERTA: Stock bajo (" + producto.getStockActual() + " unidades)\n"
                            + "Stock mínimo: " + producto.getStockMinimo(),
                            "Advertencia de Stock",
                            JOptionPane.WARNING_MESSAGE);
                }

                // Verificar si ya está en la venta
                int indiceExistente = buscarProductoEnVenta(producto.getIdProducto());
                if (indiceExistente >= 0) {

                    int i = 0;
                    NodoEnDoble<DetalleVenta> p = ventaActual.getDetalles().getPrimero();

                    while (p != null && i < indiceExistente) {
                        p = p.getSgte();
                        i++;
                    }

                    if (p == null) {
                        return;
                    }

                    DetalleVenta detalle = p.getInfo();
                    int nuevaCantidad = detalle.getCantidad() + numero;

                    if (nuevaCantidad > producto.getStockActual()) {
                        JOptionPane.showMessageDialog(this,
                                "Stock insuficiente.\nDisponible: " + producto.getStockActual()
                                + " unidades",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    detalle.setCantidad(nuevaCantidad);
                    actualizarFilaTabla(indiceExistente, detalle, producto);

                } else {
                    // Agregar nuevo producto
                    DetalleVenta nuevoDetalle = new DetalleVenta(producto, numero);
                    ventaActual.agregarDetalle(nuevoDetalle);
                    agregarFilaTabla(nuevoDetalle, producto);
                }

                actualizarTotales();
                txtCodigo1.setText("");
                txtCodigo1.requestFocus();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Producto no encontrado\nCódigo: " + codigo,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                txtCodigo1.setText("");
                txtCodigo1.requestFocus();
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
        int index = 0;
        NodoEnDoble<DetalleVenta> p = ventaActual.getDetalles().getPrimero();

        while (p != null) {
            if (p.getInfo().getIdProducto() == idProducto) {
                return index;
            }
            index++;
            p = p.getSgte();
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
            String.format("S/ %.2f", detalle.getSubTotal())
        };
        modeloDetalles.addRow(fila);
    }

    private void actualizarFilaTabla(int indice, DetalleVenta detalle, Producto producto) {
        modeloDetalles.setValueAt(detalle.getCantidad(), indice, 2);
        modeloDetalles.setValueAt(String.format("S/ %.2f", detalle.getSubTotal()), indice, 5);
    }

    private void actualizarTotales() {
        ventaActual.calcularSubtotal();
        ventaActual.calcularIgv();
        ventaActual.calcularMontoTotal();

        txtSubtotal.setText(String.format("S/ %.2f", ventaActual.getSubTotal()));

        txtIGV.setText(String.format("S/ %.2f", ventaActual.getIgv()));

        txtTotal.setText(String.format("S/ %.2f", ventaActual.getMontoTotal()));

        //usando lista enlazada doble
        int totalProductos = 0;

        if (ventaActual.getDetalles() != null && !ventaActual.getDetalles().esVacia()) {
            NodoEnDoble<DetalleVenta> p = ventaActual.getDetalles().getPrimero();

            while (p != null) {
                totalProductos += p.getInfo().getCantidad();
                p = p.getSgte();
            }
        }

        lblTotalProductos.setText("Productos: " + totalProductos + " items");
    }

    private void reiniciarVenta() {
        // Guardar cliente seleccionado si existe
        int clienteIndex = cmbCliente1.getSelectedIndex();

        // Reiniciar todo
        ventaActual = new Venta();
        ventaActual.setIdUsuario(idUsuarioActual);
        modeloDetalles.setRowCount(0);

        // Restaurar cliente si había uno seleccionado y hay elementos
        if (clienteIndex > 0 && cmbCliente1.getItemCount() > clienteIndex) {
            cmbCliente1.setSelectedIndex(clienteIndex);
        } else if (cmbCliente1.getItemCount() > 0) {
            cmbCliente1.setSelectedIndex(0);
        }

        // Actualizar número de venta
        actualizarNumeroVenta();
        actualizarTotales();

        txtCodigo1.requestFocus();
    }

    private void cancelarVenta() {
        if (!ventaActual.getDetalles().esVacia()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Cancelar venta actual?\n\n"
                    + "Se perderán todos los productos agregados ("
                    + ventaActual.getDetalles().contar() + " productos).",
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
        datos1 = new javax.swing.JPanel();
        txtCodigo1 = new javax.swing.JTextField();
        cmbCliente1 = new javax.swing.JComboBox<>();
        spnCantidad1 = new javax.swing.JSpinner(
            new SpinnerNumberModel(
                1,    // valor inicial
                1,    // mínimo
                null, // máximo (sin límite)
                1     // incremento
            )
        );
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btnEliminar1 = new javax.swing.JButton();
        btnCancelar1 = new javax.swing.JButton();
        btnVolver1 = new javax.swing.JButton();
        btnBuscar1 = new javax.swing.JButton();
        btnRegistrar1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        txtSubtotal = new javax.swing.JTextField();
        txtIGV = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        lblNumeroVenta = new javax.swing.JLabel();
        lblTotalProductos = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        datos1.setBackground(new java.awt.Color(243, 243, 243));
        datos1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 14))); // NOI18N

        txtCodigo1.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtCodigo1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N
        txtCodigo1.addActionListener(this::txtCodigo1ActionPerformed);

        cmbCliente1.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        cmbCliente1.setBorder(javax.swing.BorderFactory.createTitledBorder("Cliente"));
        cmbCliente1.addActionListener(this::cmbCliente1ActionPerformed);

        jLabel2.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        jLabel2.setText("Cantidad:");

        javax.swing.GroupLayout datos1Layout = new javax.swing.GroupLayout(datos1);
        datos1.setLayout(datos1Layout);
        datos1Layout.setHorizontalGroup(
            datos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datos1Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(txtCodigo1, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53)
                .addGroup(datos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnCantidad1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(cmbCliente1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
        );
        datos1Layout.setVerticalGroup(
            datos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datos1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(datos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtCodigo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbCliente1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(datos1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnCantidad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Montserrat ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("Ventas");

        btnEliminar1.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnEliminar1.setText("ELIMINAR PRODUCTO");
        btnEliminar1.addActionListener(this::btnEliminar1ActionPerformed);

        btnCancelar1.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnCancelar1.setText("CANCELAR VENTA");
        btnCancelar1.addActionListener(this::btnCancelar1ActionPerformed);

        btnVolver1.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVolver1.setText("VOLVER");
        btnVolver1.addActionListener(this::btnVolver1ActionPerformed);

        btnBuscar1.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnBuscar1.setText("AGREGAR PRODUCTO");
        btnBuscar1.addActionListener(this::btnBuscar1ActionPerformed);

        btnRegistrar1.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnRegistrar1.setText("REGISTRAR VENTA");
        btnRegistrar1.addActionListener(this::btnRegistrar1ActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(215, 215, 215)
                .addComponent(btnRegistrar1)
                .addGap(130, 130, 130)
                .addComponent(btnVolver1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(btnBuscar1)
                .addGap(62, 62, 62)
                .addComponent(btnEliminar1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 97, Short.MAX_VALUE)
                .addComponent(btnCancelar1)
                .addGap(80, 80, 80))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBuscar1)
                    .addComponent(btnEliminar1)
                    .addComponent(btnCancelar1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegistrar1)
                    .addComponent(btnVolver1))
                .addGap(22, 22, 22))
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
        txtSubtotal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SubTotal:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtIGV.setEditable(false);
        txtIGV.setBackground(new java.awt.Color(255, 255, 255));
        txtIGV.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtIGV.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "IGV:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtTotal.setEditable(false);
        txtTotal.setBackground(new java.awt.Color(255, 255, 255));
        txtTotal.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtTotal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        lblNumeroVenta.setBackground(new java.awt.Color(153, 153, 153));
        lblNumeroVenta.setForeground(new java.awt.Color(153, 153, 153));
        lblNumeroVenta.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblTotalProductos.setForeground(java.awt.Color.darkGray);
        lblTotalProductos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(355, 355, 355))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(125, 125, 125)
                            .addComponent(lblNumeroVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(lblTotalProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(26, 26, 26)
                            .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 761, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(jLabel1)
                .addGap(27, 27, 27)
                .addComponent(datos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTotal)
                            .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNumeroVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTotalProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtCodigo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigo1ActionPerformed
        buscarProducto();
    }//GEN-LAST:event_txtCodigo1ActionPerformed

    private void cmbCliente1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCliente1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCliente1ActionPerformed

    private void btnRegistrar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrar1ActionPerformed
        if (cmbCliente1.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un cliente para la venta",
                "Cliente Requerido",
                JOptionPane.WARNING_MESSAGE);
            cmbCliente1.requestFocus();
            return;
        }

        // Validar que haya productos
        if (ventaActual.getDetalles().esVacia()) {
            JOptionPane.showMessageDialog(this,
                "Agregue productos a la venta",
                "Venta Vacía",
                JOptionPane.WARNING_MESSAGE);
            txtCodigo1.requestFocus();
            return;
        }

        // Validar stock (segunda validación por seguridad)
        if (!ventaActual.verificarStockDisponible()) {
            JOptionPane.showMessageDialog(this,
                "Stock insuficiente para uno o más productos.\n"
                + "Revise las cantidades solicitadas.",
                "Error de Stock",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Obtener cliente seleccionado
            int indiceCliente = cmbCliente1.getSelectedIndex() - 1;
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

                NodoEnDoble<DetalleVenta> p = ventaActual.getDetalles().getPrimero();
                int i = 1;

                while (p != null) {
                    DetalleVenta detalle = p.getInfo();
                    resumen.append(String.format(
                        "%2d. %-20s x%2d = S/ %6.2f\n",
                        i++,
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getSubTotal()
                    ));
                    p = p.getSgte();
                }

                resumen.append("──────────────────────────────────────\n");
                resumen.append(String.format("Subtotal:   S/ %10.2f\n", ventaActual.getSubTotal()));
                resumen.append(String.format("IGV (18%%):  S/ %10.2f\n", ventaActual.getIgv()));
                resumen.append(String.format("TOTAL:      S/ %10.2f\n", ventaActual.getMontoTotal()));
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
                logger.info("Venta registrada exitosamente - ID: " + idVenta
                    + " - Cliente ID: " + clienteSeleccionado.getIdCliente()
                    + " - Total: S/" + ventaActual.getMontoTotal()
                    + " - Productos: " + ventaActual.getDetalles().contar());

                // Reiniciar formulario para nueva venta
                reiniciarVenta();

            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al registrar la venta en la base de datos.\n"
                    + "Por favor, intente nuevamente.",
                    "Error en Registro",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            logger.severe("Error crítico al registrar venta: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error crítico: " + ex.getMessage() + "\n"
                + "La venta no se ha registrado. Contacte al administrador.",
                "Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRegistrar1ActionPerformed

    private void btnEliminar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminar1ActionPerformed
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
    }//GEN-LAST:event_btnEliminar1ActionPerformed

    private void btnCancelar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelar1ActionPerformed
        cancelarVenta();
    }//GEN-LAST:event_btnCancelar1ActionPerformed

    private void btnVolver1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolver1ActionPerformed

        if (!ventaActual.getDetalles().esVacia()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Hay productos en la venta actual.\n"
                + "¿Desea cancelar la venta y volver al menú principal?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            } else {
                cancelarVenta();
            }
        }

        gestor.getHistorialNavegacion().navegarA("Principal");
        logger.info("Volviendo al formulario principal desde Ventas");

        if (principal != null) {
            principal.setVisible(true);
            principal.toFront();
        }

        this.setVisible(false);
    }//GEN-LAST:event_btnVolver1ActionPerformed

    private void btnBuscar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscar1ActionPerformed
        buscarProducto();
    }//GEN-LAST:event_btnBuscar1ActionPerformed

    private void tblDetallesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDetallesMouseClicked

    }//GEN-LAST:event_tblDetallesMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar1;
    private javax.swing.JButton btnCancelar1;
    private javax.swing.JButton btnEliminar1;
    private javax.swing.JButton btnRegistrar1;
    private javax.swing.JButton btnVolver1;
    private javax.swing.JComboBox<String> cmbCliente1;
    private javax.swing.JPanel datos1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblNumeroVenta;
    private javax.swing.JLabel lblTotalProductos;
    private javax.swing.JSpinner spnCantidad1;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtCodigo1;
    private javax.swing.JTextField txtIGV;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
