/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import javax.swing.JOptionPane;
import utiles.GestorSistema;
import modelo.Pedido;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import java.util.Random;
import utiles.Cola;
import utiles.Pila;
/**
 *
 * @author USER
 */
public class FrmDespacho extends javax.swing.JFrame {
    private FrmPrincipal principal;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmDespacho.class.getName());
    private GestorSistema gestor;
    
    // Estructuras de Datos
    private Cola<Pedido> colaAlmacen;
    private Pila<Pedido> pilaCamion;
    
    // Modelos visuales para las JList
    private DefaultListModel<String> modeloAlmacen;
    private DefaultListModel<String> modeloCamion;
    
    private final int CAPACIDAD_CAMION = 10;
    public FrmDespacho() {
        initComponents();
        
        // Inicializar estructuras
        colaAlmacen = new Cola<>();
        pilaCamion = new Pila<>();
        
        // Inicializar modelos visuales
        modeloAlmacen = new DefaultListModel<>();
        modeloCamion = new DefaultListModel<>();
        
        // Vincular modelos a las listas visuales
        lstAlmacen.setModel(modeloAlmacen);
        lstCamion.setModel(modeloCamion);
        
        actualizarTablas();
    }
    public void setPrincipal(FrmPrincipal principal){
        this.principal = principal;
    }
    private void actualizarTablas() {
        // 1. Limpiar listas visuales
        modeloAlmacen.clear();
        modeloCamion.clear();
        
        // 2. Llenar desde la COLA (Almacén)
        for (Pedido p : colaAlmacen.toList()) {
            modeloAlmacen.addElement("📝 Pedido #" + p.getIdPedido() + " - " + p.getClientes());
        }
        
        // 3. Llenar desde la PILA (Camión)
        for (Pedido p : pilaCamion.toList()) {
            modeloCamion.addElement("📦 CAJA #" + p.getIdPedido() + " (" + p.getClientes() + ")");
        }
        
        // 4. Actualizar etiqueta de capacidad
        lblCapacidad.setText("Capacidad: " + pilaCamion.size() + " / " + CAPACIDAD_CAMION);
        
        // Cambiar color si está lleno (Opcional)
        if (pilaCamion.size() >= CAPACIDAD_CAMION) {
            lblCapacidad.setForeground(java.awt.Color.RED);
            lblCapacidad.setText("¡CAMIÓN LLENO!");
        } else {
            lblCapacidad.setForeground(java.awt.Color.BLACK);
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstAlmacen = new javax.swing.JList<>();
        btnRecibir = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnCargar = new javax.swing.JButton();
        lblCapacidad = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstCamion = new javax.swing.JList<>();
        btnEntregar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel1.setFont(new java.awt.Font("Montserrat SemiBold", 0, 14)); // NOI18N
        jLabel1.setText("COLA DE PEDIDOS (Almacén)");

        jScrollPane1.setViewportView(lstAlmacen);

        btnRecibir.setFont(new java.awt.Font("Montserrat SemiBold", 0, 12)); // NOI18N
        btnRecibir.setText("Nuevo Pedido");
        btnRecibir.addActionListener(this::btnRecibirActionPerformed);

        jLabel2.setFont(new java.awt.Font("Montserrat SemiBold", 0, 14)); // NOI18N
        jLabel2.setText("CARGAR AL CAMIÓN → ");

        btnCargar.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnCargar.setText("CARGAR");
        btnCargar.addActionListener(this::btnCargarActionPerformed);

        lblCapacidad.setFont(new java.awt.Font("Montserrat Medium", 0, 14)); // NOI18N
        lblCapacidad.setText("Capacidad: 0/10");

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel3.setFont(new java.awt.Font("Montserrat SemiBold", 0, 14)); // NOI18N
        jLabel3.setText("INTERIOR DEL CAMIÓN (Pila)");

        jScrollPane2.setViewportView(lstCamion);

        btnEntregar.setFont(new java.awt.Font("Montserrat SemiBold", 0, 12)); // NOI18N
        btnEntregar.setText("Entregar Paquete");
        btnEntregar.addActionListener(this::btnEntregarActionPerformed);

        btnVolver.setFont(new java.awt.Font("Montserrat Medium", 0, 12)); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(this::btnVolverActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addComponent(btnRecibir, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(33, 33, 33)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(lblCapacidad, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(btnCargar))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(btnVolver)))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(btnEntregar)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addComponent(jSeparator2)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel1)
                        .addGap(41, 41, 41)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(btnRecibir))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3)
                        .addGap(36, 36, 36)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(btnEntregar)))
                .addContainerGap(63, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel2)
                .addGap(68, 68, 68)
                .addComponent(btnCargar)
                .addGap(61, 61, 61)
                .addComponent(lblCapacidad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnVolver)
                .addGap(68, 68, 68))
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
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEntregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntregarActionPerformed
        if (pilaCamion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El camión está vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // SACAMOS DE LA PILA (El último que entró es el primero en salir)
        Pedido pedidoEntregado = pilaCamion.pop();
        
        JOptionPane.showMessageDialog(this, 
            "🚚 Entregando paquete...\n" +
            "ID: " + pedidoEntregado.getIdPedido() + "\n" +
            "Cliente: " + pedidoEntregado.getClientes(), 
            "Entrega Exitosa", 
            JOptionPane.INFORMATION_MESSAGE);
            
        actualizarTablas();
    }//GEN-LAST:event_btnEntregarActionPerformed

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        gestor.getHistorialNavegacion().navegarA("Principal");
        logger.info("Volviendo al formulario principal");

        if (principal != null) {
            principal.setVisible(true);
            principal.toFront();
        }
        
        this.setVisible(false);
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnRecibirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecibirActionPerformed
       // Creamos un pedido falso para probar
        Random rand = new Random();
        Pedido p = new Pedido();
        p.setIdPedido(rand.nextInt(9000) + 1000); // ID entre 1000 y 9999
        p.setClientes("Cliente " + (rand.nextInt(50) + 1));
        
        colaAlmacen.encolar(p);
        
        actualizarTablas();
    }//GEN-LAST:event_btnRecibirActionPerformed

    private void btnCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarActionPerformed
        if (colaAlmacen.estaVacia()) {
            JOptionPane.showMessageDialog(this, "El almacén está vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (pilaCamion.size() >= CAPACIDAD_CAMION) {
            JOptionPane.showMessageDialog(this, "El camión está lleno. Debe entregar pedidos primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // SACAMOS DE LA COLA (El más antiguo)
        Pedido pedidoMover = colaAlmacen.desencolar();
        
        // METEMOS A LA PILA (Al fondo del camión)
        pilaCamion.push(pedidoMover);
        
        actualizarTablas();
    }//GEN-LAST:event_btnCargarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCargar;
    private javax.swing.JButton btnEntregar;
    private javax.swing.JButton btnRecibir;
    private javax.swing.JButton btnVolver;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblCapacidad;
    private javax.swing.JList<String> lstAlmacen;
    private javax.swing.JList<String> lstCamion;
    // End of variables declaration//GEN-END:variables
}
