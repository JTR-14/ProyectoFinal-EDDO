/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import datos.DALCategoria;
import datos.DALProductos;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Categoria;
import modelo.Producto;
import utiles.GestorSistema;

/**
 *
 * @author Toledo
 */
public class FrmProductos extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmProductos.class.getName());
    private GestorSistema gestor;
    private int idProducto, stockActual, stockMinimo, idCategoria;
    private String codigo, nombre;
    private double precioCosto, precioVenta;
    private double precioVentaAnterior; // Para registrar cambios de precio

    public FrmProductos() {
        initComponents();
        gestor = GestorSistema.getInstancia();
        llenarTabla();
        cargarCombo();
    }

    private void llenarTabla() {
        try {
            ArrayList<Producto> lista = DALProductos.obtenerProductos();
            DefaultTableModel modelo = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Hacer la tabla no editable
                }
            };
            
            modelo.addColumn("ID");          
            modelo.addColumn("Código");      
            modelo.addColumn("Nombre");      
            modelo.addColumn("P. Costo");    
            modelo.addColumn("P. Venta");    
            modelo.addColumn("Stock");       
            modelo.addColumn("Stock Min");      
            modelo.addColumn("Categoría");  // Cambiado para mostrar nombre en lugar de ID

            for (Producto producto : lista) {
                // Obtener nombre de categoría
                String nombreCategoria = obtenerNombreCategoria(producto.getIdCategoria());
                
                Object[] fila = {
                    producto.getIdProducto(),
                    producto.getCodigo(),
                    producto.getNombre(),
                    String.format("S/ %.2f", producto.getPrecioCosto()),
                    String.format("S/ %.2f", producto.getPrecioVenta()),
                    producto.getStockActual(),
                    producto.getStockMinimo(),
                    nombreCategoria
                };
                modelo.addRow(fila);
            }
            
            tblProductos.setModel(modelo);
            
            // Ajustar ancho de columnas
            if (tblProductos.getColumnModel().getColumnCount() > 0) {
                tblProductos.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
                tblProductos.getColumnModel().getColumn(1).setPreferredWidth(100);  // Código
                tblProductos.getColumnModel().getColumn(2).setPreferredWidth(150);  // Nombre
                tblProductos.getColumnModel().getColumn(3).setPreferredWidth(80);   // P. Costo
                tblProductos.getColumnModel().getColumn(4).setPreferredWidth(80);   // P. Venta
                tblProductos.getColumnModel().getColumn(5).setPreferredWidth(60);   // Stock
                tblProductos.getColumnModel().getColumn(6).setPreferredWidth(80);   // Stock Min
                tblProductos.getColumnModel().getColumn(7).setPreferredWidth(120);  // Categoría
            }
            
            logger.info("Tabla de productos cargada con " + lista.size() + " registros");
            
        } catch (Exception ex) {
            logger.severe("Error al llenar tabla de productos: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al cargar los productos: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String obtenerNombreCategoria(int idCategoria) {
        try {
            ArrayList<Categoria> categorias = DALCategoria.listarCategorias();
            for (Categoria cat : categorias) {
                if (cat.getIdCategoria() == idCategoria) {
                    return cat.getNombre();
                }
            }
        } catch (Exception ex) {
            logger.warning("Error al obtener nombre de categoría: " + ex.getMessage());
        }
        return "Desconocida";
    }

    private void cargarCombo() {
        try {
            cmbCategoria.removeAllItems();
            cmbCategoria.addItem(new Categoria(0, "-Seleccione Categoría-"));
            ArrayList<Categoria> lista = DALCategoria.listarCategorias();
            for (Categoria c : lista) {
                cmbCategoria.addItem(c);
            }
            logger.info("Combo de categorías cargado con " + lista.size() + " elementos");
        } catch (Exception ex) {
            logger.severe("Error al cargar combo de categorías: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al cargar categorías: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void registrarCambioPrecio(double precioAnterior, double precioNuevo, String motivo) {
        if (precioAnterior != precioNuevo) {
            Producto producto = new Producto();
            producto.setIdProducto(idProducto);
            producto.setNombre(nombre);
            producto.setPrecioVenta(precioNuevo);
            
            // Registrar en el historial de precios (PILA LIFO)
            gestor.getHistorialPrecios().registrarCambioPrecio(
                producto, precioAnterior, precioNuevo, motivo
            );
            
            logger.info("Cambio de precio registrado - Producto ID: " + idProducto + 
                       " - De: S/" + precioAnterior + " a S/" + precioNuevo + 
                       " - Motivo: " + motivo);
            
            // Mostrar notificación si está habilitado
            if ((Boolean)gestor.getConfiguracion("mostrar_historial")) {
                JOptionPane.showMessageDialog(this, 
                    "Cambio de precio registrado en historial:\n" +
                    "Producto: " + nombre + "\n" +
                    "Precio anterior: S/" + String.format("%.2f", precioAnterior) + "\n" +
                    "Precio nuevo: S/" + String.format("%.2f", precioNuevo) + "\n" +
                    "Motivo: " + motivo,
                    "Historial Actualizado", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void limpiar() {
        txtId.setText("");
        txtCodigo.setText("");
        txtNombre.setText("");
        txtCosto.setText("");
        txtVenta.setText("");
        txtStock.setText("");
        txtStockMin.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtCodigo.requestFocus();
        
        // Deseleccionar fila de la tabla
        tblProductos.clearSelection();
        
        // Resetear precio anterior
        precioVentaAnterior = 0;
        
        logger.fine("Campos del formulario limpiados");
    }
    
    private boolean validarCampos() {
        // Validar campos obligatorios
        if (txtCodigo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese código del producto", "AVISO", JOptionPane.WARNING_MESSAGE);
            txtCodigo.requestFocus();
            return false;
        }
        
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese nombre del producto", "AVISO", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return false;
        }
        
        // Validar campos numéricos
        try {
            precioCosto = Double.parseDouble(txtCosto.getText().trim());
            if (precioCosto <= 0) {
                JOptionPane.showMessageDialog(this, "El precio de costo debe ser mayor a 0", "AVISO", JOptionPane.WARNING_MESSAGE);
                txtCosto.requestFocus();
                return false;
            }
            
            precioVenta = Double.parseDouble(txtVenta.getText().trim());
            if (precioVenta <= 0) {
                JOptionPane.showMessageDialog(this, "El precio de venta debe ser mayor a 0", "AVISO", JOptionPane.WARNING_MESSAGE);
                txtVenta.requestFocus();
                return false;
            }
            
            // Validar que precio de venta sea mayor o igual al costo
            if (precioVenta < precioCosto) {
                int respuesta = JOptionPane.showConfirmDialog(this, 
                    "El precio de venta es menor al precio de costo.\n" +
                    "¿Desea continuar de todas formas?",
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (respuesta != JOptionPane.YES_OPTION) {
                    txtVenta.requestFocus();
                    return false;
                }
            }
            
            stockActual = Integer.parseInt(txtStock.getText().trim());
            if (stockActual < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo", "AVISO", JOptionPane.WARNING_MESSAGE);
                txtStock.requestFocus();
                return false;
            }
            
            stockMinimo = Integer.parseInt(txtStockMin.getText().trim());
            if (stockMinimo < 0) {
                JOptionPane.showMessageDialog(this, "El stock mínimo no puede ser negativo", "AVISO", JOptionPane.WARNING_MESSAGE);
                txtStockMin.requestFocus();
                return false;
            }
            
            // Validar categoría seleccionada
            if (cmbCategoria.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Seleccione categoría del producto", "AVISO", JOptionPane.WARNING_MESSAGE);
                cmbCategoria.requestFocus();
                return false;
            }
            
            // Guardar otros datos
            codigo = txtCodigo.getText().trim();
            nombre = txtNombre.getText().trim();
            Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
            this.idCategoria = categoria.getIdCategoria();
            
            return true;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Revise los campos numéricos:\n" +
                "- Precio costo\n" +
                "- Precio venta\n" +
                "- Stock actual\n" +
                "- Stock mínimo\n\n" +
                "Deben ser valores numéricos válidos.",
                "Error en datos numéricos", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtId = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        datos = new javax.swing.JPanel();
        txtCodigo = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtCosto = new javax.swing.JTextField();
        txtVenta = new javax.swing.JTextField();
        txtStock = new javax.swing.JTextField();
        txtStockMin = new javax.swing.JTextField();
        cmbCategoria = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        datos.setBackground(new java.awt.Color(243, 243, 243));
        datos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 14))); // NOI18N

        txtCodigo.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtCodigo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtNombre.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtNombre.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nombre:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtCosto.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtCosto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Precio Costo:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtVenta.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtVenta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Precio Venta:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtStock.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtStock.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Stock Actual:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtStockMin.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtStockMin.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Stock Minimo:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        cmbCategoria.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        cmbCategoria.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Categoría:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        javax.swing.GroupLayout datosLayout = new javax.swing.GroupLayout(datos);
        datos.setLayout(datosLayout);
        datosLayout.setHorizontalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCosto, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 157, Short.MAX_VALUE)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbCategoria, javax.swing.GroupLayout.Alignment.TRAILING, 0, 211, Short.MAX_VALUE)
                    .addComponent(txtStockMin, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtStock, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(71, 71, 71))
        );
        datosLayout.setVerticalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, datosLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)))
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(txtCosto, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33))
                    .addGroup(datosLayout.createSequentialGroup()
                        .addComponent(txtStockMin, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)))
                .addComponent(txtVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Montserrat ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("Productos");

        btnGuardar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        btnModificar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnModificar.setText("MODIFICAR");
        btnModificar.addActionListener(this::btnModificarActionPerformed);

        btnEliminar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnEliminar.setText("ELIMINAR");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);

        btnLimpiar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

        btnVolver.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(this::btnVolverActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(btnGuardar)
                .addGap(18, 18, 18)
                .addComponent(btnModificar)
                .addGap(38, 38, 38)
                .addComponent(btnEliminar)
                .addGap(32, 32, 32)
                .addComponent(btnLimpiar)
                .addGap(26, 26, 26)
                .addComponent(btnVolver)
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnModificar)
                    .addComponent(btnEliminar)
                    .addComponent(btnLimpiar)
                    .addComponent(btnVolver))
                .addGap(21, 21, 21))
        );

        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProductos);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(datos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 667, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(289, 289, 289)
                        .addComponent(jLabel1)))
                .addContainerGap(33, Short.MAX_VALUE))
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
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        if (!validarCampos()) {
            return;
        }
        
        try {
            if (DALProductos.insertarProducto(codigo, nombre, precioCosto, precioVenta, 
                                              stockActual, stockMinimo, idCategoria)) {
                JOptionPane.showMessageDialog(this, 
                    "Producto registrado correctamente", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                logger.info("Producto registrado - Código: " + codigo + " - Nombre: " + nombre);
                llenarTabla();
                limpiar();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo registrar el producto (puede que el código ya exista)", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            logger.severe("Error al registrar producto: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al registrar producto: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un producto de la tabla para modificar", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Guardar precio anterior antes de validar
        try {
            precioVentaAnterior = Double.parseDouble(txtVenta.getText().trim());
        } catch (NumberFormatException e) {
            precioVentaAnterior = 0;
        }
        
        if (!validarCampos()) {
            return;
        }
        
        try {
            idProducto = Integer.parseInt(txtId.getText());
            
            // Registrar cambio de precio si aplica
            if (precioVenta != precioVentaAnterior && precioVentaAnterior > 0) {
                registrarCambioPrecio(precioVentaAnterior, precioVenta, "Modificación manual");
            }
            
            if (DALProductos.modificarProducto(idProducto, codigo, nombre, precioCosto, 
                                              precioVenta, stockActual, stockMinimo, idCategoria)) {
                JOptionPane.showMessageDialog(this, 
                    "Producto modificado correctamente", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                logger.info("Producto modificado - ID: " + idProducto + " - Nombre: " + nombre);
                llenarTabla();
                limpiar();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo modificar el producto", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "ID de producto inválido", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            logger.severe("Error al modificar producto: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al modificar producto: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un producto de la tabla para eliminar", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de eliminar este producto?\n\n" +
            "ADVERTENCIA: Si hay ventas asociadas a este producto,\n" +
            "no podrá ser eliminado.", 
            "Confirmar Eliminación", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                idProducto = Integer.parseInt(txtId.getText());
                
                if (DALProductos.eliminarProducto(idProducto)) {
                    JOptionPane.showMessageDialog(this, 
                        "Producto eliminado correctamente", 
                        "Éxito", 
                        JOptionPane.INFORMATION_MESSAGE);
                    logger.info("Producto eliminado - ID: " + idProducto);
                    llenarTabla();
                    limpiar();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No se pudo eliminar el producto (puede tener ventas asociadas)", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "ID de producto inválido", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                logger.severe("Error al eliminar producto: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Error al eliminar producto: " + ex.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void tblProductosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductosMouseClicked
        int fila = tblProductos.getSelectedRow();
        
        if (fila >= 0) {
            try {
                txtId.setText(tblProductos.getValueAt(fila, 0).toString());
                txtCodigo.setText(tblProductos.getValueAt(fila, 1).toString());
                txtNombre.setText(tblProductos.getValueAt(fila, 2).toString());
                
                // Limpiar el símbolo "S/" y espacios antes de asignar
                String precioCostoStr = tblProductos.getValueAt(fila, 3).toString().replace("S/", "").trim();
                String precioVentaStr = tblProductos.getValueAt(fila, 4).toString().replace("S/", "").trim();
                
                txtCosto.setText(precioCostoStr);
                txtVenta.setText(precioVentaStr);
                txtStock.setText(tblProductos.getValueAt(fila, 5).toString());
                txtStockMin.setText(tblProductos.getValueAt(fila, 6).toString());
                
                // Guardar precio anterior para comparación
                precioVentaAnterior = Double.parseDouble(precioVentaStr);
                
                // Buscar y seleccionar la categoría correcta
                String nombreCategoria = tblProductos.getValueAt(fila, 7).toString();
                for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                    Categoria item = cmbCategoria.getItemAt(i);
                    if (item.getNombre().equals(nombreCategoria)) {
                        cmbCategoria.setSelectedIndex(i);
                        break;
                    }
                }
                
                logger.fine("Producto seleccionado - ID: " + txtId.getText() + " - Nombre: " + txtNombre.getText());
                
            } catch (Exception ex) {
                logger.warning("Error al cargar datos del producto seleccionado: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Error al cargar datos del producto: " + ex.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_tblProductosMouseClicked

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        gestor.getHistorialNavegacion().navegarA("Principal");
        logger.info("Volviendo al formulario principal");
        
        // Cerrar este formulario
        this.dispose();
        
        // Volver al principal si hay usuario logueado
        if (gestor.getUsuarioActual() != null) {
            java.awt.EventQueue.invokeLater(() -> {
                new FrmPrincipal(gestor.getUsuarioActual()).setVisible(true);
            });
        }
    }//GEN-LAST:event_btnVolverActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<Categoria> cmbCategoria;
    private javax.swing.JPanel datos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtCosto;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtStock;
    private javax.swing.JTextField txtStockMin;
    private javax.swing.JTextField txtVenta;
    // End of variables declaration//GEN-END:variables

}
