/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;


public class Pila<T> {
    
    private NodoPila<T> cima; 
    private int tamano;       

    public Pila() {
        this.cima = null;
        this.tamano = 0;
    }

    public void push(T dato) {
        NodoPila<T> nuevo = new NodoPila<>(dato);
        nuevo.setSiguiente(cima);
        cima = nuevo;
        tamano++;
    }

    public T pop() {
        if (isEmpty()) {
            return null;
        }
        
        T dato = cima.getDato();
        cima = cima.getSiguiente();
        tamano--;
        
        return dato;
    }

    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return cima.getDato();
    }

    public boolean isEmpty() {
        return cima == null;
    }

    public int size() {
        return tamano;
    }

    public void clear() {
        cima = null;
        tamano = 0;
    }
    public java.util.List<T> toList() {
        java.util.List<T> lista = new java.util.ArrayList<>();
        NodoPila<T> actual = cima;
        while (actual != null) {
            lista.add(actual.getDato());
            actual = actual.getSiguiente();
        }
        return lista;
    }

}
