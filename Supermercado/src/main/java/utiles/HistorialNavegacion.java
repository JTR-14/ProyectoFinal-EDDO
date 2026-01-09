/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

/**
 *
 * @author USER
 */

import java.util.Stack;

public class HistorialNavegacion {
    private Stack<String> historial;
    private Stack<String> futuro; // Para el botón "adelante"
    
    public HistorialNavegacion() {
        historial = new Stack<>();
        futuro = new Stack<>();
    }
    
    // Navegar a una nueva página
    public void navegarA(String pagina) {
        if (!historial.isEmpty() && !historial.peek().equals(pagina)) {
            historial.push(pagina);
            futuro.clear(); // Al navegar a nueva página, limpiamos el futuro
            System.out.println("Navegando a: " + pagina);
        } else if (historial.isEmpty()) {
            historial.push(pagina);
            System.out.println("Navegando a: " + pagina);
        }
    }
    
    // Retroceder
    public String retroceder() {
        if (historial.size() <= 1) {
            System.out.println("No hay páginas anteriores");
            return null;
        }
        
        String paginaActual = historial.pop();
        futuro.push(paginaActual);
        
        String paginaAnterior = historial.peek();
        System.out.println("Retrocediendo a: " + paginaAnterior);
        
        return paginaAnterior;
    }
    
    // Adelantar
    public String adelantar() {
        if (futuro.isEmpty()) {
            System.out.println("No hay páginas siguientes");
            return null;
        }
        
        String paginaFutura = futuro.pop();
        historial.push(paginaFutura);
        
        System.out.println("Adelantando a: " + paginaFutura);
        return paginaFutura;
    }
    
    // Ver página actual
    public String verPaginaActual() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.peek();
    }
    
    // Métodos de utilidad
    public void mostrarHistorial() {
        System.out.println("=== HISTORIAL DE NAVEGACIÓN ===");
        System.out.println("(La cima es la página actual)");
        
        Stack<String> copia = (Stack<String>) historial.clone();
        Stack<String> temp = new Stack<>();
        
        // Invertir para mostrar en orden cronológico
        while (!copia.isEmpty()) {
            temp.push(copia.pop());
        }
        
        int posicion = 1;
        while (!temp.isEmpty()) {
            System.out.println(posicion++ + ". " + temp.pop());
        }
        
        System.out.println("===============================");
    }
    
    public void limpiarHistorial() {
        historial.clear();
        futuro.clear();
        System.out.println("Historial de navegación limpiado");
    }
    
    // Getters
    public Stack<String> getHistorial() {
        return historial;
    }
    
    public Stack<String> getFuturo() {
        return futuro;
    }
}