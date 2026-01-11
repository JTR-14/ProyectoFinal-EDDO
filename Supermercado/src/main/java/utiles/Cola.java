/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

import java.util.ArrayList;
import java.util.List;

public class Cola<T> {
    
    private NodoCola<T> frente;
    private NodoCola<T> finalCola;
    private int tamano;

    public Cola() {
        this.frente = null;
        this.finalCola = null;
        this.tamano = 0;
    }

    public void encolar(T dato) {
        NodoCola<T> nuevo = new NodoCola<>(dato);
        
        if (estaVacia()) {
            frente = nuevo;
            finalCola = nuevo;
        } else {
            finalCola.setSiguiente(nuevo);
            finalCola = nuevo;
        }
        tamano++;
    }

    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        
        if (frente == null) {
            finalCola = null;
        }
        
        tamano--;
        return dato;
    }

    public T verFrente() {
        if (estaVacia()) {
            return null;
        }
        return frente.getDato();
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int tamano() {
        return tamano;
    }

    public void limpiar() {
        frente = null;
        finalCola = null;
        tamano = 0;
    }
    
    public List<T> toList() {
        List<T> lista = new ArrayList<>();
        NodoCola<T> actual = frente;
        while (actual != null) {
            lista.add(actual.getDato());
            actual = actual.getSiguiente();
        }
        return lista;
    }
}