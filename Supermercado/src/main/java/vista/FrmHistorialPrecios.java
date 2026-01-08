/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import datos.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.*;
import utiles.GestorSistema;
import utiles.HistorialCambiosPrecio;

/**
 *
 * @author Toledo
 */
public class FrmHistorialPrecios extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmHistorialPrecios.class.getName());
    private GestorSistema gestor;
    private HistorialCambiosPrecio historial;
    private DefaultTableModel modeloHistorial;
    private List<Producto> listaProductos;
    
    public FrmHistorialPrecios() {
        initComponents();
        gestor = GestorSistema.getInstancia();
        historial = gestor.getHistorialPrecios();
        cargarProductos();
        inicializarTablaHistorial();
        cargarHistorialCompleto();
        actualizarEstadisticas();
    }
    
    private void cargarProductos() {
        try {
            listaProductos = DALProductos.obtenerProductos();
            cmbProducto.removeAllItems();
            cmbProducto.addItem("-- Todos los Productos --");
            
            for (Producto producto : listaProductos) {
                cmbProducto.addItem(producto.getNombre() + " (ID: " + producto.getIdProducto() + ")");
            }
            
            logger.info("Productos cargados para historial: " + listaProductos.size());
        } catch (Exception ex) {
            logger.severe("Error al cargar productos: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void inicializarTablaHistorial() {
        modeloHistorial = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // ID Cambio
                if (columnIndex == 1) return Integer.class; // ID Producto
                return String.class;
            }
        };
        
        modeloHistorial.addColumn("ID Cambio");
        modeloHistorial.addColumn("ID Producto");
        modeloHistorial.addColumn("Producto");
        modeloHistorial.addColumn("Precio Anterior");
        modeloHistorial.addColumn("Precio Nuevo");
        modeloHistorial.addColumn("Fecha");
        modeloHistorial.addColumn("Motivo");
        modeloHistorial.addColumn("Usuario");
        
        tblHistorial.setModel(modeloHistorial);
        
        // Ajustar ancho de columnas
        if (tblHistorial.getColumnModel().getColumnCount() > 0) {
            tblHistorial.getColumnModel().getColumn(0).setPreferredWidth(70);   // ID Cambio
            tblHistorial.getColumnModel().getColumn(1).setPreferredWidth(70);   // ID Producto
            tblHistorial.getColumnModel().getColumn(2).setPreferredWidth(150);  // Producto
            tblHistorial.getColumnModel().getColumn(3).setPreferredWidth(90);   // Precio Anterior
            tblHistorial.getColumnModel().getColumn(4).setPreferredWidth(90);   // Precio Nuevo
            tblHistorial.getColumnModel().getColumn(5).setPreferredWidth(120);  // Fecha
            tblHistorial.getColumnModel().getColumn(6).setPreferredWidth(150);  // Motivo
            tblHistorial.getColumnModel().getColumn(7).setPreferredWidth(120);  // Usuario
        }
    }
    
    private void cargarHistorialCompleto() {
        modeloHistorial.setRowCount(0);
        
        List<HistorialCambiosPrecio.CambioPrecio> cambios = historial.obtenerTodosLosCambios();
        
        for (HistorialCambiosPrecio.CambioPrecio cambio : cambios) {
            modeloHistorial.addRow(cambio.toTableRow());
        }
        
        lblTotalRegistros.setText("Total registros: " + cambios.size());
        logger.info("Historial cargado: " + cambios.size() + " registros");
    }
    
    private void buscarPorProducto() {
        if (cmbProducto.getSelectedIndex() == 0) {
            cargarHistorialCompleto();
            return;
        }
        
        try {
            // Obtener ID del producto seleccionado
            String seleccion = cmbProducto.getSelectedItem().toString();
            int startIndex = seleccion.lastIndexOf("(ID: ") + 5;
            int endIndex = seleccion.lastIndexOf(")");
            String idStr = seleccion.substring(startIndex, endIndex);
            int idProducto = Integer.parseInt(idStr);
            
            // Buscar cambios para este producto
            List<HistorialCambiosPrecio.CambioPrecio> cambios = historial.buscarCambiosPorProducto(idProducto);
            
            modeloHistorial.setRowCount(0);
            for (HistorialCambiosPrecio.CambioPrecio cambio : cambios) {
                modeloHistorial.addRow(cambio.toTableRow());
            }
            
            lblTotalRegistros.setText("Registros encontrados: " + cambios.size());
            logger.info("Búsqueda por producto - ID: " + idProducto + " - Resultados: " + cambios.size());
            
        } catch (Exception ex) {
            logger.severe("Error al buscar por producto: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al buscar por producto: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buscarPorFecha() {
        String fechaInicio = txtFechaInicio.getText().trim();
        String fechaFin = txtFechaFin.getText().trim();
        
        if (fechaInicio.isEmpty() || fechaFin.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingrese ambas fechas para la búsqueda",
                "Fechas Requeridas",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Buscar cambios por rango de fecha
            List<HistorialCambiosPrecio.CambioPrecio> cambios = historial.buscarCambiosPorFecha(fechaInicio, fechaFin);
            
            modeloHistorial.setRowCount(0);
            for (HistorialCambiosPrecio.CambioPrecio cambio : cambios) {
                modeloHistorial.addRow(cambio.toTableRow());
            }
            
            lblTotalRegistros.setText("Registros encontrados: " + cambios.size() + 
                                    " (" + fechaInicio + " a " + fechaFin + ")");
            logger.info("Búsqueda por fecha - Desde: " + fechaInicio + " Hasta: " + fechaFin + 
                       " - Resultados: " + cambios.size());
            
        } catch (Exception ex) {
            logger.severe("Error al buscar por fecha: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al buscar por fecha: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void verDetallesCambio() {
        int fila = tblHistorial.getSelectedRow();
        if (fila >= 0) {
            try {
                int idCambio = (Integer) tblHistorial.getValueAt(fila, 0);
                
                // Buscar el cambio seleccionado
                List<HistorialCambiosPrecio.CambioPrecio> cambios = historial.obtenerTodosLosCambios();
                HistorialCambiosPrecio.CambioPrecio cambioSeleccionado = null;
                
                for (HistorialCambiosPrecio.CambioPrecio cambio : cambios) {
                    if (cambio.getIdCambio() == idCambio) {
                        cambioSeleccionado = cambio;
                        break;
                    }
                }
                
                if (cambioSeleccionado != null) {
                    // Mostrar detalles en un cuadro de diálogo
                    JOptionPane.showMessageDialog(this,
                        cambioSeleccionado.toDetailedString(),
                        "Detalles del Cambio #" + idCambio,
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
            } catch (Exception ex) {
                logger.warning("Error al mostrar detalles del cambio: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                    "Error al mostrar detalles: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Seleccione un registro para ver sus detalles",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void verUltimoCambio() {
        HistorialCambiosPrecio.CambioPrecio ultimoCambio = historial.verUltimoCambio();
        
        if (ultimoCambio != null) {
            JOptionPane.showMessageDialog(this,
                ultimoCambio.toDetailedString(),
                "Último Cambio Registrado",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "No hay cambios registrados en el historial",
                "Historial Vacío",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deshacerUltimoCambio() {
        if (historial.estaVacia()) {
            JOptionPane.showMessageDialog(this,
                "No hay cambios para deshacer",
                "Historial Vacío",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Deshacer el último cambio de precio registrado?\n\n" +
            "Esta acción revertirá el último cambio de precio\n" +
            "pero no afectará los precios actuales de los productos.",
            "Confirmar Deshacer",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            HistorialCambiosPrecio.CambioPrecio cambioDeshecho = historial.deshacerUltimoCambio();
            
            if (cambioDeshecho != null) {
                JOptionPane.showMessageDialog(this,
                    "✅ ÚLTIMO CAMBIO DESHECHO\n\n" +
                    "Se ha eliminado del historial:\n" +
                    "Producto: " + cambioDeshecho.getProducto().getNombre() + "\n" +
                    "Precio anterior: S/" + String.format("%.2f", cambioDeshecho.getPrecioAnterior()) + "\n" +
                    "Precio nuevo: S/" + String.format("%.2f", cambioDeshecho.getPrecioNuevo()) + "\n" +
                    "Motivo: " + cambioDeshecho.getMotivo(),
                    "Cambio Deshecho",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Actualizar tabla
                cargarHistorialCompleto();
                actualizarEstadisticas();
                
                logger.info("Último cambio deshecho - ID: " + cambioDeshecho.getIdCambio());
            }
        }
    }
    
    private void actualizarEstadisticas() {
        HistorialCambiosPrecio.EstadisticasPrecios stats = historial.obtenerEstadisticas();
        
        lblTotalCambios.setText("Total: " + stats.getTotalCambios());
        lblAumentos.setText("Aumentos: " + stats.getAumentos());
        lblDisminuciones.setText("Disminuciones: " + stats.getDisminuciones());
        
        // Actualizar colores según estadísticas
        if (stats.getAumentos() > stats.getDisminuciones()) {
            lblAumentos.setForeground(new java.awt.Color(0, 102, 0)); // Verde
            lblDisminuciones.setForeground(new java.awt.Color(102, 102, 102)); // Gris
        } else if (stats.getDisminuciones() > stats.getAumentos()) {
            lblAumentos.setForeground(new java.awt.Color(102, 102, 102)); // Gris
            lblDisminuciones.setForeground(new java.awt.Color(204, 0, 0)); // Rojo
        } else {
            lblAumentos.setForeground(new java.awt.Color(102, 102, 102)); // Gris
            lblDisminuciones.setForeground(new java.awt.Color(102, 102, 102)); // Gris
        }
    }
    
    private void mostrarEstadisticasCompletas() {
        HistorialCambiosPrecio.EstadisticasPrecios stats = historial.obtenerEstadisticas();
        
        JOptionPane.showMessageDialog(this,
            stats.toString(),
            "Estadísticas de Cambios de Precio",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportarHistorial() {
        String contenido = historial.exportarATexto();
        
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(30, 80);
        textArea.setText(contenido);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
        
        int opcion = JOptionPane.showConfirmDialog(this,
            scrollPane,
            "Historial de Precios - Exportar",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (opcion == JOptionPane.YES_OPTION) {
            // Aquí se podría agregar lógica para guardar en archivo
            JOptionPane.showMessageDialog(this,
                "El historial se ha preparado para exportar.\n" +
                "Copia el contenido y guárdalo en un archivo de texto.",
                "Exportación Lista",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void limpiarFiltros() {
        cmbProducto.setSelectedIndex(0);
        txtFechaInicio.setText("");
        txtFechaFin.setText("");
        cargarHistorialCompleto();
    }
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtId = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        datos = new javax.swing.JPanel();
        txtFechaFin = new javax.swing.JTextField();
        txtFechaInicio = new javax.swing.JTextField();
        cmbProducto = new javax.swing.JComboBox<>();
        lblTotalRegistros = new javax.swing.JLabel();
        lblTotalCambios = new javax.swing.JLabel();
        lblDisminuciones = new javax.swing.JLabel();
        lblAumentos = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnBuscarFecha = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnEstadisticas = new javax.swing.JButton();
        btnBuscarProducto = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        btnExportar = new javax.swing.JButton();
        btnDeshacer = new javax.swing.JButton();
        btnVerDetalles = new javax.swing.JButton();
        btnVerUltimo = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHistorial = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        datos.setBackground(new java.awt.Color(243, 243, 243));
        datos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 14))); // NOI18N

        txtFechaFin.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtFechaFin.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Fecha final:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        txtFechaInicio.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        txtFechaInicio.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Fecha inicio:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        cmbProducto.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        cmbProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Producto:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N

        lblTotalRegistros.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblTotalCambios.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblDisminuciones.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblAumentos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout datosLayout = new javax.swing.GroupLayout(datos);
        datos.setLayout(datosLayout);
        datosLayout.setHorizontalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFechaFin, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(73, 73, 73)
                        .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTotalRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTotalCambios, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAumentos, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDisminuciones, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(cmbProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        datosLayout.setVerticalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDisminuciones, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtFechaFin, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalCambios, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAumentos, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addComponent(cmbProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Montserrat ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("Historial Precios");

        btnBuscarFecha.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnBuscarFecha.setText("BUSCAR FECHA");
        btnBuscarFecha.addActionListener(this::btnBuscarFechaActionPerformed);

        btnLimpiar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

        btnEstadisticas.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnEstadisticas.setText("ESTADÍSTICAS");
        btnEstadisticas.addActionListener(this::btnEstadisticasActionPerformed);

        btnBuscarProducto.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnBuscarProducto.setText("BUSCAR PRODUCTO");
        btnBuscarProducto.addActionListener(this::btnBuscarProductoActionPerformed);

        btnVolver.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(this::btnVolverActionPerformed);

        btnExportar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnExportar.setText("EXPORTAR");
        btnExportar.addActionListener(this::btnExportarActionPerformed);

        btnDeshacer.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnDeshacer.setText("DESHACER");
        btnDeshacer.addActionListener(this::btnDeshacerActionPerformed);

        btnVerDetalles.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVerDetalles.setText("VER DETALLES");
        btnVerDetalles.addActionListener(this::btnVerDetallesActionPerformed);

        btnVerUltimo.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVerUltimo.setText("VER ÚLTIMO");
        btnVerUltimo.addActionListener(this::btnVerUltimoActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(btnVerDetalles)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiar)
                        .addGap(39, 39, 39)
                        .addComponent(btnVerUltimo)
                        .addGap(51, 51, 51)
                        .addComponent(btnVolver))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(btnBuscarFecha)
                        .addGap(18, 18, 18)
                        .addComponent(btnBuscarProducto)
                        .addGap(18, 18, 18)
                        .addComponent(btnEstadisticas)
                        .addGap(18, 18, 18)
                        .addComponent(btnExportar)
                        .addGap(18, 18, 18)
                        .addComponent(btnDeshacer)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEstadisticas)
                    .addComponent(btnBuscarProducto)
                    .addComponent(btnBuscarFecha)
                    .addComponent(btnExportar)
                    .addComponent(btnDeshacer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVolver)
                    .addComponent(btnVerDetalles)
                    .addComponent(btnVerUltimo)
                    .addComponent(btnLimpiar))
                .addGap(21, 21, 21))
        );

        tblHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHistorialMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblHistorial);

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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(datos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGap(0, 60, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(datos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarProductoActionPerformed
        buscarPorProducto();
    }//GEN-LAST:event_btnBuscarProductoActionPerformed

    private void btnBuscarFechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarFechaActionPerformed
        buscarPorFecha();
    }//GEN-LAST:event_btnBuscarFechaActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarFiltros();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnEstadisticasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEstadisticasActionPerformed
        mostrarEstadisticasCompletas();
    }//GEN-LAST:event_btnEstadisticasActionPerformed

    private void tblHistorialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblHistorialMouseClicked
        
    }//GEN-LAST:event_tblHistorialMouseClicked

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        gestor.getHistorialNavegacion().navegarA("Principal");
        logger.info("Volviendo al formulario principal desde Historial de Precios");
        
        // Cerrar este formulario
        this.dispose();
        
        // Volver al principal si hay usuario logueado
        if (gestor.getUsuarioActual() != null) {
            java.awt.EventQueue.invokeLater(() -> {
                new FrmPrincipal(gestor.getUsuarioActual()).setVisible(true);
            });
        }
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnExportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarActionPerformed
        exportarHistorial();
    }//GEN-LAST:event_btnExportarActionPerformed

    private void btnDeshacerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeshacerActionPerformed
        deshacerUltimoCambio();
    }//GEN-LAST:event_btnDeshacerActionPerformed

    private void btnVerDetallesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerDetallesActionPerformed
        verDetallesCambio();
    }//GEN-LAST:event_btnVerDetallesActionPerformed

    private void btnVerUltimoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerUltimoActionPerformed
        verUltimoCambio();
    }//GEN-LAST:event_btnVerUltimoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarFecha;
    private javax.swing.JButton btnBuscarProducto;
    private javax.swing.JButton btnDeshacer;
    private javax.swing.JButton btnEstadisticas;
    private javax.swing.JButton btnExportar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnVerDetalles;
    private javax.swing.JButton btnVerUltimo;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<String> cmbProducto;
    private javax.swing.JPanel datos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAumentos;
    private javax.swing.JLabel lblDisminuciones;
    private javax.swing.JLabel lblTotalCambios;
    private javax.swing.JLabel lblTotalRegistros;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTextField txtFechaFin;
    private javax.swing.JTextField txtFechaInicio;
    private javax.swing.JTextField txtId;
    // End of variables declaration//GEN-END:variables

}
