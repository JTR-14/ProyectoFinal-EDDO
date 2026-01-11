/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import datos.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.*;
import utiles.GestorSistema;
import utiles.HistorialCambiosPrecio;
import java.util.List;

public class FrmHistorialPrecios extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmHistorialPrecios.class.getName());
    private GestorSistema gestor;
    private HistorialCambiosPrecio historial;
    private DefaultTableModel modeloHistorial;
    private List<Producto> listaProductos;
    private FrmPrincipal principal;
    
    public FrmHistorialPrecios() {
        initComponents();
        gestor = GestorSistema.getInstancia();
        historial = gestor.getHistorialPrecios();
        cargarProductos();
        inicializarTablaHistorial();
        cargarHistorialCompleto();
        actualizarEstadisticas();
        cargarFechasIniciales();
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
    
    // CORRECCIÓN: Método setPrincipal con parámetro
    public void setPrincipal(FrmPrincipal principal) {
        this.principal = principal;
    }
    
    private void inicializarTablaHistorial() {
        modeloHistorial = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 1) return Integer.class;
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
        
        if (tblHistorial.getColumnModel().getColumnCount() > 0) {
            tblHistorial.getColumnModel().getColumn(0).setPreferredWidth(70);
            tblHistorial.getColumnModel().getColumn(1).setPreferredWidth(70);
            tblHistorial.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblHistorial.getColumnModel().getColumn(3).setPreferredWidth(90);
            tblHistorial.getColumnModel().getColumn(4).setPreferredWidth(90);
            tblHistorial.getColumnModel().getColumn(5).setPreferredWidth(120);
            tblHistorial.getColumnModel().getColumn(6).setPreferredWidth(150);
            tblHistorial.getColumnModel().getColumn(7).setPreferredWidth(120);
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
            String seleccion = cmbProducto.getSelectedItem().toString();
            int startIndex = seleccion.lastIndexOf("(ID: ") + 5;
            int endIndex = seleccion.lastIndexOf(")");
            String idStr = seleccion.substring(startIndex, endIndex);
            int idProducto = Integer.parseInt(idStr);
            
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
    // 1. Validar que se haya seleccionado algo en todos los combos
    if (cmbDiaInicio.getSelectedItem() == null || cmbMesInicio.getSelectedItem() == null || cmbAnioInicio.getSelectedItem() == null ||
        cmbDiaFin.getSelectedItem() == null || cmbMesFin.getSelectedItem() == null || cmbAnioFin.getSelectedItem() == null) {
        
        JOptionPane.showMessageDialog(this,
            "Por favor, configure las fechas completas (Día, Mes y Año)",
            "Fechas Incompletas",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    // 2. Construir la fecha de INICIO (Formato: "YYYY-MM-DD")
    String anioInicio = (String) cmbAnioInicio.getSelectedItem();
    String mesInicio = obtenerNumeroMes((String) cmbMesInicio.getSelectedItem());
    String diaInicio = (String) cmbDiaInicio.getSelectedItem();
    
    String fechaInicio = anioInicio + "-" + mesInicio + "-" + diaInicio;

    // 3. Construir la fecha de FIN (Formato: "YYYY-MM-DD")
    String anioFin = (String) cmbAnioFin.getSelectedItem();
    String mesFin = obtenerNumeroMes((String) cmbMesFin.getSelectedItem());
    String diaFin = (String) cmbDiaFin.getSelectedItem();
    
    String fechaFin = anioFin + "-" + mesFin + "-" + diaFin;

    // --- El resto de tu lógica se mantiene igual ---
    try {
        // Llamada al método de búsqueda con los Strings ya formateados
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
    private String obtenerNumeroMes(String nombreMes) {
    if (nombreMes == null) return "01"; // Protección por defecto
    
    switch (nombreMes) {
        case "Enero": return "01";
        case "Febrero": return "02";
        case "Marzo": return "03";
        case "Abril": return "04";
        case "Mayo": return "05";
        case "Junio": return "06";
        case "Julio": return "07";
        case "Agosto": return "08";
        case "Septiembre": return "09";
        case "Octubre": return "10";
        case "Noviembre": return "11";
        case "Diciembre": return "12";
        default: return "01";
    }
}
    
    private void verDetallesCambio() {
        int fila = tblHistorial.getSelectedRow();
        if (fila >= 0) {
            try {
                int idCambio = (Integer) tblHistorial.getValueAt(fila, 0);
                
                List<HistorialCambiosPrecio.CambioPrecio> cambios = historial.obtenerTodosLosCambios();
                HistorialCambiosPrecio.CambioPrecio cambioSeleccionado = null;
                
                for (HistorialCambiosPrecio.CambioPrecio cambio : cambios) {
                    if (cambio.getIdCambio() == idCambio) {
                        cambioSeleccionado = cambio;
                        break;
                    }
                }
                
                if (cambioSeleccionado != null) {
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
        
        if (stats.getAumentos() > stats.getDisminuciones()) {
            lblAumentos.setForeground(new java.awt.Color(0, 102, 0));
            lblDisminuciones.setForeground(new java.awt.Color(102, 102, 102));
        } else if (stats.getDisminuciones() > stats.getAumentos()) {
            lblAumentos.setForeground(new java.awt.Color(102, 102, 102));
            lblDisminuciones.setForeground(new java.awt.Color(204, 0, 0));
        } else {
            lblAumentos.setForeground(new java.awt.Color(102, 102, 102));
            lblDisminuciones.setForeground(new java.awt.Color(102, 102, 102));
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
            JOptionPane.showMessageDialog(this,
                "El historial se ha preparado para exportar.\n" +
                "Copia el contenido y guárdalo en un archivo de texto.",
                "Exportación Lista",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void limpiarFiltros() {
        cmbProducto.setSelectedIndex(0);
        cmbMesInicio.setSelectedIndex(0);
        cmbDiaInicio.setSelectedIndex(0);
        cmbAnioInicio.setSelectedIndex(0);
        cmbMesFin.setSelectedIndex(0);
        cmbDiaFin.setSelectedIndex(0);
        cmbAnioFin.setSelectedIndex(0);
        cargarHistorialCompleto();
    }
private void cargarFechasIniciales() {
    String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                      "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    
    // 1. Llenar Meses
    for (String mes : meses) {
        cmbMesInicio.addItem(mes);
        cmbMesFin.addItem(mes);
    }
    
    // 2. Llenar Años (Ejemplo: Del año actual - 5 hasta el año actual + 5)
    int anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    for (int i = anioActual - 5; i <= anioActual + 5; i++) {
        cmbAnioInicio.addItem(String.valueOf(i));
        cmbAnioFin.addItem(String.valueOf(i));
    }
    
    // 3. Seleccionar fechas por defecto (Opcional)
    cmbAnioInicio.setSelectedItem(String.valueOf(anioActual));
    cmbAnioFin.setSelectedItem(String.valueOf(anioActual));
    
    // 4. Calcular los días por primera vez
    actualizarDias(cmbAnioInicio, cmbMesInicio, cmbDiaInicio);
    actualizarDias(cmbAnioFin, cmbMesFin, cmbDiaFin);
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtId = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        datos = new javax.swing.JPanel();
        cmbProducto = new javax.swing.JComboBox<>();
        lblTotalRegistros = new javax.swing.JLabel();
        lblTotalCambios = new javax.swing.JLabel();
        lblDisminuciones = new javax.swing.JLabel();
        lblAumentos = new javax.swing.JLabel();
        cmbMesInicio = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        cmbDiaInicio = new javax.swing.JComboBox<>();
        cmbAnioInicio = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        cmbMesFin = new javax.swing.JComboBox<>();
        cmbDiaFin = new javax.swing.JComboBox<>();
        cmbAnioFin = new javax.swing.JComboBox<>();
        btnBuscarFecha = new javax.swing.JButton();
        btnBuscarProducto = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnLimpiar = new javax.swing.JButton();
        btnEstadisticas = new javax.swing.JButton();
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

        cmbProducto.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        cmbProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Producto:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Montserrat Medium", 0, 12))); // NOI18N
        cmbProducto.addActionListener(this::cmbProductoActionPerformed);

        lblTotalRegistros.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblTotalCambios.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblDisminuciones.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblAumentos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        cmbMesInicio.setBorder(javax.swing.BorderFactory.createTitledBorder("Mes:"));
        cmbMesInicio.addActionListener(this::cmbMesInicioActionPerformed);

        jLabel2.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        jLabel2.setText("Fecha Inicio:");

        cmbDiaInicio.setBorder(javax.swing.BorderFactory.createTitledBorder("Dia:"));

        cmbAnioInicio.setBorder(javax.swing.BorderFactory.createTitledBorder("Año:"));
        cmbAnioInicio.addActionListener(this::cmbAnioInicioActionPerformed);

        jLabel3.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        jLabel3.setText("Fecha Fin:");

        cmbMesFin.setBorder(javax.swing.BorderFactory.createTitledBorder("Mes:"));
        cmbMesFin.addActionListener(this::cmbMesFinActionPerformed);

        cmbDiaFin.setBorder(javax.swing.BorderFactory.createTitledBorder("Dia:"));

        cmbAnioFin.setBorder(javax.swing.BorderFactory.createTitledBorder("Año:"));
        cmbAnioFin.addActionListener(this::cmbAnioFinActionPerformed);

        btnBuscarFecha.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnBuscarFecha.setText("BUSCAR FECHA");
        btnBuscarFecha.addActionListener(this::btnBuscarFechaActionPerformed);

        btnBuscarProducto.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnBuscarProducto.setText("BUSCAR PRODUCTO");
        btnBuscarProducto.addActionListener(this::btnBuscarProductoActionPerformed);

        javax.swing.GroupLayout datosLayout = new javax.swing.GroupLayout(datos);
        datos.setLayout(datosLayout);
        datosLayout.setHorizontalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(datosLayout.createSequentialGroup()
                                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(datosLayout.createSequentialGroup()
                                        .addComponent(cmbAnioFin, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(cmbMesFin, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel2)
                                    .addGroup(datosLayout.createSequentialGroup()
                                        .addComponent(cmbAnioInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(cmbMesInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbDiaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbDiaFin, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(datosLayout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(btnBuscarFecha)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 128, Short.MAX_VALUE)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblTotalRegistros, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTotalCambios, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmbProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblAumentos, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDisminuciones, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarProducto))
                .addGap(34, 34, 34))
        );
        datosLayout.setVerticalGroup(
            datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datosLayout.createSequentialGroup()
                        .addComponent(lblTotalRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)
                        .addComponent(lblTotalCambios, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(datosLayout.createSequentialGroup()
                        .addComponent(lblAumentos, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblDisminuciones, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27))
            .addGroup(datosLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbDiaInicio)
                    .addComponent(cmbAnioInicio)
                    .addComponent(cmbMesInicio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(datosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbMesFin)
                    .addComponent(cmbDiaFin)
                    .addComponent(cmbAnioFin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnBuscarFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
        );

        jLabel1.setFont(new java.awt.Font("Montserrat ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("Historial Precios");

        btnLimpiar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

        btnEstadisticas.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnEstadisticas.setText("ESTADÍSTICAS DE PRECIOS");
        btnEstadisticas.addActionListener(this::btnEstadisticasActionPerformed);

        btnVolver.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(this::btnVolverActionPerformed);

        btnExportar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnExportar.setText("INFORME DE CAMBIOS DE PRECIO");
        btnExportar.addActionListener(this::btnExportarActionPerformed);

        btnDeshacer.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnDeshacer.setText("DESHACER ULTIMO CAMBIO");
        btnDeshacer.addActionListener(this::btnDeshacerActionPerformed);

        btnVerDetalles.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVerDetalles.setText("VER DETALLES");
        btnVerDetalles.addActionListener(this::btnVerDetallesActionPerformed);

        btnVerUltimo.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVerUltimo.setText("VER ÚLTIMO CAMBIO");
        btnVerUltimo.addActionListener(this::btnVerUltimoActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(88, 88, 88)
                        .addComponent(btnVerUltimo)
                        .addGap(87, 87, 87))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(btnEstadisticas)
                        .addGap(53, 53, 53)
                        .addComponent(btnVerDetalles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnDeshacer)
                        .addGap(121, 121, 121)
                        .addComponent(btnVolver))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(btnLimpiar)
                        .addGap(56, 56, 56)
                        .addComponent(btnExportar)))
                .addContainerGap(143, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEstadisticas)
                    .addComponent(btnLimpiar)
                    .addComponent(btnVerDetalles)
                    .addComponent(btnExportar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVerUltimo)
                    .addComponent(btnDeshacer)
                    .addComponent(btnVolver))
                .addContainerGap())
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(407, 407, 407)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(datos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(datos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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
        logger.info("Volviendo al formulario principal");

        if (principal != null) {
            principal.setVisible(true);
            principal.toFront();
        }
        
        this.setVisible(false);
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

    private void cmbMesInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMesInicioActionPerformed
        actualizarDias(cmbAnioInicio, cmbMesInicio, cmbDiaInicio);
    }//GEN-LAST:event_cmbMesInicioActionPerformed

    private void cmbAnioInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAnioInicioActionPerformed
       actualizarDias(cmbAnioInicio, cmbMesInicio, cmbDiaInicio);
    }//GEN-LAST:event_cmbAnioInicioActionPerformed

    private void cmbMesFinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMesFinActionPerformed
        actualizarDias(cmbAnioFin, cmbMesFin, cmbDiaFin);
    }//GEN-LAST:event_cmbMesFinActionPerformed

    private void cmbAnioFinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAnioFinActionPerformed
        actualizarDias(cmbAnioFin, cmbMesFin, cmbDiaFin);
    }//GEN-LAST:event_cmbAnioFinActionPerformed

    private void cmbProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbProductoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbProductoActionPerformed
// Método maestro para calcular días dinámicamente
private void actualizarDias(javax.swing.JComboBox<String> cmbAnio, javax.swing.JComboBox<String> cmbMes, javax.swing.JComboBox<String> cmbDia) {
    // 1. Validar que haya datos seleccionados
    if (cmbAnio.getSelectedItem() == null || cmbMes.getSelectedItem() == null) {
        return;
    }

    // 2. Guardar el día que estaba seleccionado antes de borrar todo (para restaurarlo si es posible)
    String diaSeleccionadoPrevio = (String) cmbDia.getSelectedItem();

    // 3. Obtener Año y Mes numéricos
    int anio = Integer.parseInt((String) cmbAnio.getSelectedItem());
    int mesIndex = cmbMes.getSelectedIndex(); // 0=Enero, 1=Febrero...
    
    // 4. Calcular cuántos días tiene ese mes
    int diasEnMes = 0;
    
    switch (mesIndex) {
        case 0: // Enero
        case 2: // Marzo
        case 4: // Mayo
        case 6: // Julio
        case 7: // Agosto
        case 9: // Octubre
        case 11: // Diciembre
            diasEnMes = 31;
            break;
        case 3: // Abril
        case 5: // Junio
        case 8: // Septiembre
        case 10: // Noviembre
            diasEnMes = 30;
            break;
        case 1: // Febrero (El complicado)
            // Algoritmo de año bisiesto
            if ((anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0)) {
                diasEnMes = 29;
            } else {
                diasEnMes = 28;
            }
            break;
        default:
            diasEnMes = 30;
    }

    // 5. Llenar el ComboBox de Días
    cmbDia.removeAllItems(); // Borrar los anteriores
    for (int i = 1; i <= diasEnMes; i++) {
        // Agregamos el 0 a la izquierda para que se vea bonito (01, 02... 10)
        cmbDia.addItem(String.format("%02d", i));
    }

    // 6. Intentar restaurar la selección previa (UX)
    // Si antes tenías seleccionado el día 30 y cambias a Febrero, se pondrá el 1 automáticamente.
    // Pero si tenías el 5 y cambias de mes, se mantendrá en el 5.
    if (diaSeleccionadoPrevio != null) {
        int diaPrevio = Integer.parseInt(diaSeleccionadoPrevio);
        if (diaPrevio <= diasEnMes) {
            cmbDia.setSelectedItem(diaSeleccionadoPrevio);
        }
    }
}

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
    private javax.swing.JComboBox<String> cmbAnioFin;
    private javax.swing.JComboBox<String> cmbAnioInicio;
    private javax.swing.JComboBox<String> cmbDiaFin;
    private javax.swing.JComboBox<String> cmbDiaInicio;
    private javax.swing.JComboBox<String> cmbMesFin;
    private javax.swing.JComboBox<String> cmbMesInicio;
    private javax.swing.JComboBox<String> cmbProducto;
    private javax.swing.JPanel datos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAumentos;
    private javax.swing.JLabel lblDisminuciones;
    private javax.swing.JLabel lblTotalCambios;
    private javax.swing.JLabel lblTotalRegistros;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTextField txtId;
    // End of variables declaration//GEN-END:variables

}
