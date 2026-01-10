/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

/**
 *
 * @author USER
 */
public class ArbolAVL<T extends Comparable<T>> {
    private NodoAVL<T> raiz;

    public ArbolAVL() {
        this.raiz = null;
    }

    private int altura(NodoAVL<T> N) {
        return (N == null) ? 0 : N.getAltura();
    }

    private int max(int a, int b) {
        return (a > b) ? a : b;
    }

    private int getBalance(NodoAVL<T> N) {
        return (N == null) ? 0 : altura(N.getIzquierda()) - altura(N.getDerecha());
    }

    private NodoAVL<T> rotacionDerecha(NodoAVL<T> y) {
        NodoAVL<T> x = y.getIzquierda();
        NodoAVL<T> T2 = x.getDerecha();

        x.setDerecha(y);
        y.setIzquierda(T2);

        y.setAltura(max(altura(y.getIzquierda()), altura(y.getDerecha())) + 1);
        x.setAltura(max(altura(x.getIzquierda()), altura(x.getDerecha())) + 1);

        return x;
    }

    private NodoAVL<T> rotacionIzquierda(NodoAVL<T> x) {
        NodoAVL<T> y = x.getDerecha();
        NodoAVL<T> T2 = y.getIzquierda();

        y.setIzquierda(x);
        x.setDerecha(T2);

        x.setAltura(max(altura(x.getIzquierda()), altura(x.getDerecha())) + 1);
        y.setAltura(max(altura(y.getIzquierda()), altura(y.getDerecha())) + 1);

        return y;
    }

    public void insertar(T dato) {
        raiz = insertarRec(raiz, dato);
    }

    private NodoAVL<T> insertarRec(NodoAVL<T> nodo, T dato) {
        if (nodo == null) return new NodoAVL<>(dato);

        int comparacion = dato.compareTo(nodo.getDato());

        if (comparacion < 0)
            nodo.setIzquierda(insertarRec(nodo.getIzquierda(), dato));
        else if (comparacion > 0)
            nodo.setDerecha(insertarRec(nodo.getDerecha(), dato));
        else
            return nodo;

        nodo.setAltura(1 + max(altura(nodo.getIzquierda()), altura(nodo.getDerecha())));

        int balance = getBalance(nodo);

        if (balance > 1 && dato.compareTo(nodo.getIzquierda().getDato()) < 0)
            return rotacionDerecha(nodo);

        if (balance < -1 && dato.compareTo(nodo.getDerecha().getDato()) > 0)
            return rotacionIzquierda(nodo);

        if (balance > 1 && dato.compareTo(nodo.getIzquierda().getDato()) > 0) {
            nodo.setIzquierda(rotacionIzquierda(nodo.getIzquierda()));
            return rotacionDerecha(nodo);
        }

        if (balance < -1 && dato.compareTo(nodo.getDerecha().getDato()) < 0) {
            nodo.setDerecha(rotacionDerecha(nodo.getDerecha()));
            return rotacionIzquierda(nodo);
        }

        return nodo;
    }


    public T buscar(T datoBusqueda) {
        NodoAVL<T> res = buscarRec(raiz, datoBusqueda);
        return (res != null) ? res.getDato() : null;
    }

    private NodoAVL<T> buscarRec(NodoAVL<T> nodo, T datoBusqueda) {
        if (nodo == null) return null;
        
        int comparacion = datoBusqueda.compareTo(nodo.getDato());

        if (comparacion == 0) return nodo; 
        
        if (comparacion < 0) return buscarRec(nodo.getIzquierda(), datoBusqueda);
        
        return buscarRec(nodo.getDerecha(), datoBusqueda);
    }
    
    public void limpiar() { 
        raiz = null; 
    }
}
